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

package de.gematik.bbriccs.fhir.validation;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import java.util.ArrayList;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class ValidationMessageUtil {

  public static ValidationResult prependWithInfo(ValidationResult vr, String msg) {
    val svmList = new ArrayList<>(vr.getMessages());
    svmList.add(0, createSingleValidationMessage(ResultSeverityEnum.INFORMATION, msg));
    return new ValidationResult(vr.getContext(), svmList);
  }

  public static SingleValidationMessage createInfoMessage(String msg) {
    return createSingleValidationMessage(ResultSeverityEnum.INFORMATION, msg);
  }

  public static SingleValidationMessage createErrorMessage(String msg) {
    return createSingleValidationMessage(ResultSeverityEnum.ERROR, msg);
  }

  public static SingleValidationMessage createSingleValidationMessage(
      ResultSeverityEnum severity, String msg) {
    val info = new SingleValidationMessage();
    info.setMessage(msg);
    info.setSeverity(severity);
    return info;
  }
}
