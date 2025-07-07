/*
 * Copyright 2025 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.bbriccs.smartcards.cli.cmd;

import static java.text.MessageFormat.format;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.cli.param.InputOutputDirectoryParameter;
import de.gematik.bbriccs.cli.utils.FileWalker;
import de.gematik.bbriccs.smartcards.*;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.bbriccs.smartcards.exceptions.SmartcardFactoryException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

@Command(
    name = "archive",
    description = "Create Smartcards Archive for usage with smartcards-brick",
    mixinStandardHelpOptions = true)
@Slf4j
public class ArchiveSmartcards implements Callable<Integer> {

  private static final Pattern ICCSN_PATTERN = Pattern.compile("^(\\d{15,20})$");

  @Mixin protected InputOutputDirectoryParameter inputOutputDirectory;

  @Override
  public Integer call() throws Exception {
    log.info("Archive Smartcards from {}", inputOutputDirectory.getInputDirectory().toString());

    List<SmartcardConfigDto> configs = new LinkedList<>();

    this.walkSmartcardsDirectory("egk", SmartcardType.EGK, this::createEGKConfig).stream()
        .collect(Collectors.toCollection(() -> configs));

    this.walkSmartcardsDirectory("smcb", SmartcardType.SMC_B, this::createSmcbConfig).stream()
        .collect(Collectors.toCollection(() -> configs));

    this.walkSmartcardsDirectory("hba", SmartcardType.HBA, this::createHBAConfig).stream()
        .collect(Collectors.toCollection(() -> configs));

    val om =
        new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writerWithDefaultPrettyPrinter();
    val out = om.writeValueAsString(configs);

    this.inputOutputDirectory.writeFile("smartcards.json", out);
    return 0;
  }

  private List<SmartcardConfigDto> walkSmartcardsDirectory(
      String dirName, SmartcardType type, Function<Path, SmartcardConfigDto> creator) {
    val basePath = inputOutputDirectory.getInputDirectory().resolve(dirName);
    if (isWalkableDirectory(basePath)) {
      val smartcardWalker = new FileWalker(basePath, false);
      val smartcardDirs = smartcardWalker.find(FileWalker.WalkType.DIRECTORIES);
      return smartcardDirs.stream().map(creator).toList();
    } else {
      log.warn(
          format(
              "Directory {0} is not walkable: no {1}-Smartcards will be read",
              basePath.toAbsolutePath(), type.name()));
      return List.of();
    }
  }

  private SmartcardConfigDto createEGKConfig(Path path) {
    val readCertificates = readCertificates(path);
    val dto = readCertificates.getRight();
    dto.setType(SmartcardType.EGK);
    val certificates = readCertificates.getLeft();

    val egk = new EgkP12(dto, certificates);
    val owner = egk.getOwnerData();
    dto.setIdentifier(egk.getKvnr());
    dto.setOwnerName(owner.getCommonName());
    return dto;
  }

  private SmartcardConfigDto createSmcbConfig(Path path) {
    val readCertificates = readCertificates(path);
    val dto = readCertificates.getRight();
    dto.setType(SmartcardType.SMC_B);
    val certificates = readCertificates.getLeft();

    val smcB = new SmcBP12(dto, certificates);
    val owner = smcB.getOwnerData();
    dto.setOwnerName(owner.getCommonName());

    return dto;
  }

  private SmartcardConfigDto createHBAConfig(Path path) {
    val readCertificates = readCertificates(path);
    val dto = readCertificates.getRight();
    dto.setType(SmartcardType.HBA);
    val certificates = readCertificates.getLeft();

    val hba = new HbaP12(dto, certificates);
    val owner = hba.getOwnerData();
    dto.setOwnerName(owner.getCommonName());

    return dto;
  }

  private Pair<List<SmartcardCertificate>, SmartcardConfigDto> readCertificates(Path path) {
    log.info("Read Certificates from: {}", path.toAbsolutePath());
    val walker = new FileWalker(path, true);
    val dto = new SmartcardConfigDto();

    val iccsn = path.toFile().getName(); // by convention, the directory name is the ICCSN
    if (!ICCSN_PATTERN.matcher(iccsn).matches()) {
      throw new IllegalArgumentException(
          format(
              "Convention violation: given diretory does not conform ICCSN structure: {0}",
              path.toAbsolutePath()));
    }

    val certificateFiles =
        walker.find(FileWalker.WalkType.FILES).stream()
            .map(Path::toFile)
            .filter(fp -> fp.getName().endsWith(KeystoreType.P12.getFileExtension()))
            .toList();

    val certificates =
        certificateFiles.stream()
            .map(
                f -> {
                  Supplier<InputStream> fiss =
                      () -> {
                        try {
                          return Files.newInputStream(f.toPath());
                        } catch (IOException e) {
                          throw new SmartcardFactoryException(
                              format("Unable to load File from {0}", f), e);
                        }
                      };
                  return (SmartcardCertificate) new SmartcardCertificateP12(f.getPath(), fiss);
                })
            .toList();

    dto.setIccsn(iccsn);
    dto.setStores(
        certificateFiles.stream()
            .map(fp -> createRelativePath(path.getParent().getParent(), fp))
            .toList());

    return Pair.of(certificates, dto);
  }

  private boolean isWalkableDirectory(Path path) {
    val f = path.toFile();
    return f.exists() && f.isDirectory() && f.canRead();
  }

  private String createRelativePath(Path base, File certificateFile) {
    return base.relativize(certificateFile.toPath()).toString();
  }
}
