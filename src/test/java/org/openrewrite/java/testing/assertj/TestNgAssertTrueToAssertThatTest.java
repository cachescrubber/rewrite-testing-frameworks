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

@SuppressWarnings({"ConstantConditions", "ExcessiveLambdaUsage"})
class TestNgAssertTrueToAssertThatTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .parser(JavaParser.fromJavaVersion()
            .classpathFromResources(new InMemoryExecutionContext(), "assertj-core-3.24", "testng-7.7.1"))
          .recipe(new TestNgAssertTrueToAssertThat());
    }


    @DocumentExample
    @Test
    void singleStaticMethodNoMessage() {
        //language=java
        rewriteRun(
          java(
            """
              import org.testng.annotations.Test;
                                         
              import static org.testng.Assert.assertTrue;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertTrue(notification() != null && notification() > 0);
                  }
                  private Integer notification() {
                      return 1;
                  }
              }
              """,
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.assertThat;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification() != null && notification() > 0).isTrue();
                  }
                  private Integer notification() {
                      return 1;
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
                                         
              import static org.testng.Assert.*;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertTrue(notification() != null && notification() > 0, "The notification should be positive");
                  }
                  private Integer notification() {
                      return 1;
                  }
              }
              """,
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.assertThat;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification() != null && notification() > 0).as("The notification should be positive").isTrue();
                  }
                  private Integer notification() {
                      return 1;
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
                      org.testng.Assert.assertTrue(notification() != null && notification() > 0);
                      org.testng.Assert.assertTrue(notification() != null && notification() > 0, "The notification should be positive");
                  }
                  private Integer notification() {
                      return 1;
                  }
              }
              """,
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.assertThat;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification() != null && notification() > 0).isTrue();
                      assertThat(notification() != null && notification() > 0).as("The notification should be positive").isTrue();
                  }
                  private Integer notification() {
                      return 1;
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
              import static org.testng.Assert.assertTrue;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertTrue(notification() != null && notification() > 0);
                      org.testng.Assert.assertTrue(notification() != null && notification() > 0, "The notification should be positive");
                  }
                  private Integer notification() {
                      return 1;
                  }
              }
              """,
            """
              import org.testng.annotations.Test;
                            
              import static org.assertj.core.api.Assertions.*;
                            
              public class MyTest {
                  @Test
                  public void test() {
                      assertThat(notification() != null && notification() > 0).isTrue();
                      assertThat(notification() != null && notification() > 0).as("The notification should be positive").isTrue();
                  }
                  private Integer notification() {
                      return 1;
                  }
              }
              """
          )
        );
    }

}
