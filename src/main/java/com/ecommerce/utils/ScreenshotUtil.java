package com.ecommerce.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {
    public static String takeScreenshot(WebDriver driver, String name) {
        try {
            if (!(driver instanceof TakesScreenshot)) return null;
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            String tsStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
            Path outDirTarget = Path.of("target", "screenshots");
            Files.createDirectories(outDirTarget);
            Path outTarget = outDirTarget.resolve(name + "_" + tsStr + ".png");
            Files.copy(src.toPath(), outTarget, StandardCopyOption.REPLACE_EXISTING);
            return outTarget.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
