package bio.fkaiser.fit3d.controller;

import bio.fkaiser.fit3d.model.TemplateBasedJob;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author fk
 */
@RestController
@RequestMapping("/api")
public class JobController {

    @RequestMapping(value = "/create")
    public TemplateBasedJob create() {
        TemplateBasedJob templateBasedJob = new TemplateBasedJob();
        templateBasedJob.setId(UUID.randomUUID().toString());
        return templateBasedJob;
    }
}
