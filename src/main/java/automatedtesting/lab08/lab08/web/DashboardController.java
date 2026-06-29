package automatedtesting.lab08.lab08.web;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import automatedtesting.lab08.lab08.config.CurrentUser;
import automatedtesting.lab08.lab08.model.FileEntity;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.StorageService;
import automatedtesting.lab08.lab08.service.UploadInput;

/**
 * A thin web UI so Playwright can drive a real browser: it renders the quota,
 * lists the user's files and lets them upload one. Requires authentication,
 * like every other non-public route.
 */
@Controller
public class DashboardController {

    private static final double MB = 1024d * 1024d;

    private final CurrentUser currentUser;
    private final StorageService storage;

    public DashboardController(CurrentUser currentUser, StorageService storage) {
        this.currentUser = currentUser;
        this.storage = storage;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        User user = currentUser.require();
        model.addAttribute("displayName", user.getDisplayName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("quota", formatMb(user.getQuotaBytes()));
        model.addAttribute("used", formatMb(storage.usedBytes(user)));
        model.addAttribute("free", formatMb(storage.freeBytes(user)));
        model.addAttribute("files", storage.listAll(user).stream().map(FileEntity::getName).toList());
        return "dashboard";
    }

    /** Upload from the browser form, then redirect back to the dashboard (R6). */
    @PostMapping("/dashboard/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        User user = currentUser.require();
        if (file != null && !file.isEmpty()) {
            String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
            storage.upload(user, new UploadInput(name, file.getBytes()));
        }
        return "redirect:/dashboard";
    }

    /** 52428800 -> "50 MB" (whole-number MB shown without decimals). */
    private String formatMb(long bytes) {
        double mb = bytes / MB;
        if (mb == Math.floor(mb)) {
            return (long) mb + " MB";
        }
        return String.format("%.1f MB", mb);
    }
}
