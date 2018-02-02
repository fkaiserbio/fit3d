package bio.fkaiser.fit3d.controller;

import bio.fkaiser.fit3d.model.TemplateBasedJob;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/submit")
public class SubmitController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void upload(@RequestParam("file") MultipartFile file, @RequestParam("id") String id) {
        System.out.println(file + " for id " + id);
    }

    @RequestMapping(value = "/template-based", method = RequestMethod.POST)
    public void submit(@RequestBody TemplateBasedJob job) {
        System.out.println("received template-based job " + job);
    }
}
