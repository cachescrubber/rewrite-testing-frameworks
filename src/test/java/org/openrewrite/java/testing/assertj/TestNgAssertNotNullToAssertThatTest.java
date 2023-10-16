/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.testing.assertj;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

@SuppressWarnings({"ExcessiveLambdaUsage"})
class TestNgAssertNotNullToAssertThatTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .parser(JavaParser.fromJavaVersion()
            .classpathFromResources(new InMemoryExecutionContext(), "testng-7.7.1"))
          .recipe(new TestNgAssertNotNullToAssertThat());
    }

    @DocumentExample
    @Test
    void singleStaticMethodNoMessage() {
        //language=java
        rewriteRun(
          java(
            """
              import static org.testng.Assert.assertNotNull;
              import java.time.LocalDate;

              import org.testng.annotations.Test;

              public class MyTest {
                  @Test
                  public void test() {
                      LocalDate newDate = LocalDate.now();
                      assertNotNull(newDate);
                  }
                  private String notification() {
                      return "";
                  }
              }
              """,
            """
              import java.time.LocalDate;

              import org.testng.annotations.Test;

              import static org.assertj.core.api.Assertions.assertThat;

              public class MyTest {
                  @Test
                  public void test() {
                      LocalDate newDate = LocalDate.now();
                      assertThat(newDate).isNotNull();
                  }
                  private String notification() {
                      return "";
                  }
              }
              """
          )
        );
    }

    @Test
    void singleStaticMethodWithMessageString() {
        //language=java
        rewriteRun(
          spec -> spec.typeValidationOptions(TypeValidation.none()),
          java(
            """
              import org.testng.annotations.Test;

              import static org.testng.Assert.assertNotNull;

              public class MyTest {
                  @Test
                  public void test() {
                      assertNotNull(notification(), "Should not be null");
                  }
                  private String notification() {
                      return "";
                  }
              }
              """,
            """
              import org.testng.annotations.Test;

              import static org.assertj.core.api.Assertions.assertThat;

              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification()).as("Should not be null").isNotNull();
                  }
                  private String notification() {
                      return "";
                  }
              }
              """
          )
        );
    }

    @Test
    void inlineReference() {
        //language=java
        rewriteRun(
          spec -> spec.typeValidationOptions(TypeValidation.none()),
          java(
            """
              import org.testng.annotations.Test;

              public class MyTest {
                  @Test
                  public void test() {
                      org.testng.Assert.assertNotNull(notification());
                      org.testng.Assert.assertNotNull(notification(), "Should not be null");
                  }
                  private String notification() {
                      return "";
                  }
              }
              """,
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.assertThat;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification()).isNotNull();
                      assertThat(notification()).as("Should not be null").isNotNull();
                  }
                  private String notification() {
                      return "";
                  }
              }
              """
          )
        );
    }

    @Test
    void mixedReferences() {
        //language=java
        rewriteRun(
          spec -> spec.typeValidationOptions(TypeValidation.none()),
          java(
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.*;
              import static org.testng.Assert.assertNotNull;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertNotNull(notification());
                      org.testng.Assert.assertNotNull(notification(), "Should not be null");
                  }
                  private String notification() {
                      return "";
                  }
              }
              """,
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.*;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification()).isNotNull();
                      assertThat(notification()).as("Should not be null").isNotNull();
                  }
                  private String notification() {
                      return "";
                  }
              }
              """
          )
        );
    }
}
