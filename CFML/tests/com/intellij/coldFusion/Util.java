/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.UsefulTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by Lera Nikolaenko
 */
public class Util {
    @NonNls
    private static final String INPUT_DATA_FILE_EXT = "test.cfml";
    @NonNls
    private static final String EXPECTED_RESULT_FILE_EXT = "test.expected";

    public static String getInputDataFilePath(final String dataSubpath, final String testName) {
        return getInputDataFilePath(dataSubpath, testName, INPUT_DATA_FILE_EXT);
    }

    public static String getExpectedDataFilePath(final String dataSubpath, final String testName) {
        return getInputDataFilePath(dataSubpath, testName, EXPECTED_RESULT_FILE_EXT);
    }

    public static String getInputDataFileName(final String testName) {
        return testName + "." + INPUT_DATA_FILE_EXT;
    }

    public static String getExpectedDataFileName(final String testName) {
        return testName + "." + EXPECTED_RESULT_FILE_EXT;
    }

    public static String getInputData(final String dataSubpath, final String testName) {
        return getFileText(getInputDataFilePath(dataSubpath, testName, INPUT_DATA_FILE_EXT));
    }

    private static String getInputDataFilePath(final String dataSubpath, final String testName, final String fileExtension) {
        return PathManager.getHomePath() + "/" + dataSubpath + "/" + testName + "." + fileExtension;
    }

    private static String getFileText(final String filePath) {
        try {
            return FileUtil.loadFile(new File(filePath));
        } catch (IOException e) {
            System.out.println(filePath);
            throw new RuntimeException(e);
        }
    }

    public static void runTestWithLanguageLevel(Callable<Void> callable, String testLanguageLevel, Project project) throws Exception {
      CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
      final String languageLevel = currentState.getLanguageLevel();
      try {
        currentState.setLanguageLevel(testLanguageLevel);
        CfmlProjectConfiguration.getInstance(project).loadState(currentState);
        callable.call();
      }
      finally {
        currentState.setLanguageLevel(languageLevel);
        CfmlProjectConfiguration.getInstance(project).loadState(currentState);
      }
    }

  static void doParserTest(String testNameLowercased, Project project, String extraDataPath) throws IOException {
    String fileName = testNameLowercased + ".cfml";

    String testText = StringUtil.convertLineSeparators(loadFile(extraDataPath + testNameLowercased + ".test.cfml"));
    final PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, testText);
    final String tree = DebugUtil.psiTreeToString(psiFile, true);

    UsefulTestCase.assertSameLinesWithFile(extraDataPath + testNameLowercased + ".test.expected", tree);
  }

  private static String loadFile(String fileName) throws IOException {
    return FileUtil.loadFile(new File(FileUtil.toSystemDependentName(fileName)));
  }
}
