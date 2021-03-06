/*
 * Copyright (C) 2016 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.internal.codegen;

import static dagger.internal.codegen.BindingElementValidator.AllowsMultibindings.NO_MULTIBINDINGS;
import static dagger.internal.codegen.BindingElementValidator.AllowsScoping.NO_SCOPING;
import static dagger.internal.codegen.BindingMethodValidator.Abstractness.MUST_BE_ABSTRACT;
import static dagger.internal.codegen.BindingMethodValidator.ExceptionSuperclass.NO_EXCEPTIONS;
import static dagger.internal.codegen.InjectionAnnotations.getQualifiers;
import static dagger.internal.codegen.InjectionAnnotations.injectedConstructors;
import static dagger.internal.codegen.Keys.isValidImplicitProvisionKey;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.producers.ProducerModule;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/** A validator for {@link BindsOptionalOf} methods. */
final class BindsOptionalOfMethodValidator extends BindingMethodValidator {

  private final DaggerTypes types;

  @Inject
  BindsOptionalOfMethodValidator(
      DaggerElements elements,
      DaggerTypes types,
      DependencyRequestValidator dependencyRequestValidator) {
    super(
        elements,
        types,
        BindsOptionalOf.class,
        ImmutableSet.of(Module.class, ProducerModule.class),
        dependencyRequestValidator,
        MUST_BE_ABSTRACT,
        NO_EXCEPTIONS,
        NO_MULTIBINDINGS,
        NO_SCOPING);
    this.types = types;
  }

  @Override
  protected void checkElement(ValidationReport.Builder<ExecutableElement> builder) {
    super.checkElement(builder);
    checkParameters(builder);
  }

  @Override
  protected void checkKeyType(
      ValidationReport.Builder<ExecutableElement> builder, TypeMirror keyType) {
    super.checkKeyType(builder, keyType);
    if (isValidImplicitProvisionKey(
            getQualifiers(builder.getSubject()).stream().findFirst(), keyType, types)
        && !injectedConstructors(MoreElements.asType(MoreTypes.asDeclared(keyType).asElement()))
            .isEmpty()) {
      builder.addError(
          "@BindsOptionalOf methods cannot return unqualified types that have an @Inject-"
              + "annotated constructor because those are always present");
    }
  }

  @Override
  protected void checkParameters(ValidationReport.Builder<ExecutableElement> builder) {
    if (!builder.getSubject().getParameters().isEmpty()) {
      builder.addError("@BindsOptionalOf methods cannot have parameters");
    }
  }
}
