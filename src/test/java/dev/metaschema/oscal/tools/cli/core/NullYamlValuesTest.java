/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.metaschema.core.util.ObjectUtils;
import dev.metaschema.databind.io.Format;
import dev.metaschema.databind.io.IBoundLoader;
import dev.metaschema.oscal.lib.OscalBindingContext;
import dev.metaschema.oscal.lib.model.AssessmentResults;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

class NullYamlValuesTest {
  private static OscalBindingContext bindingContext;
  private static IBoundLoader loader;

  @BeforeAll
  static void initialize() {
    bindingContext = OscalBindingContext.instance();
    loader = bindingContext.newBoundLoader();
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar1() throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    AssessmentResults data
        = loader.load(
            ObjectUtils.requireNonNull(Paths.get("src/test/resources/yaml-null/example_ar_nullvar-1.yaml")));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);

    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar2() throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    AssessmentResults data
        = loader.load(
            ObjectUtils.requireNonNull(Paths.get("src/test/resources/yaml-null/example_ar_nullvar-2.yaml")));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);

    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar3() throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    AssessmentResults data
        = loader.load(
            ObjectUtils.requireNonNull(Paths.get("src/test/resources/yaml-null/example_ar_nullvar-3.yaml")));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);

    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

}
