package tz.co.fasthub.ona.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tz.co.fasthub.ona.component.TalentValidator;
import tz.co.fasthub.ona.domain.Talent;
import tz.co.fasthub.ona.service.TalentService;

import javax.validation.Valid;

@Controller
public class TalentController {

    private TalentService talentService;

    @Autowired
    TalentValidator talentValidator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setTalentService(TalentService talentService) {
        this.talentService = talentService;
    }

    private JavaMailSender javaMailSender;

    public TalentValidator getTalentValidator(){
        return talentValidator;
    }

    public void setTalentValidator(TalentValidator talentValidator){
        this.talentValidator=talentValidator;
    }

    @Autowired
    public TalentController(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // List all talents

    @RequestMapping(value = "/talents", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("talents", talentService.listAllTalent());
        return "talent/talents";
    }

    // View a specific talent by its id

    @RequestMapping("talent/{id}")
    public String showTalent(@PathVariable Integer id, Model model) {
        model.addAttribute("talent", talentService.getTalentById(id));
        return "talent/talentshow";
    }

    @RequestMapping("talent/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("talent", talentService.getTalentById(id));
        return "talent/talenteditform";
    }

    // New talent

    @RequestMapping("talent/new")
    public String newTalent(Model model) {
        model.addAttribute("talent", new Talent());
        return "talent/talentform";
    }

    // Save talent to database

    @RequestMapping(value = "talent", method = RequestMethod.POST)
    public String saveTalent(@Valid Talent talent, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("Talent", talent);
        talentValidator.validate(talent,result);
        if(result.hasErrors()){
            return "talent/talentform";
        }

        talent.setPassword(passwordEncoder.encode(talent.getPassword()));
        
        talentService.saveTalent(talent);
        try {
            sendMail(talent.getEmail(), "WELCOME TO ONA PLATFORM", "Hello " + talent.getFname() + " " + talent.getLname() + ",\n\nThank you for being a part of Binary by Agrrey & Clifford. Looking forward to working with you. \n\n\n Best Regards, \n\n The Binary Team");
        } catch (MailException me)
        {
            redirectAttributes.addFlashAttribute("flash.message", "Email not sent! " +me.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("flash.message", "Uncaught Exception: " + e.getMessage());
        }

        return "redirect:/talent/" + talent.getId();

    }

    private void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

    // Delete talent by its id

    @RequestMapping("talent/delete/{id}")
    public String delete(@PathVariable Integer id) {
        talentService.deleteTalent(id);
        return "redirect:/talents";
    }

}

