package sbs.controller.hrua;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import sbs.controller.upload.UploadController;
import sbs.service.hrua.HrUaService;
import sbs.service.users.UserService;

@Controller
@RequestMapping("hrua")
public class HrUaController {

	@Autowired
	UserService userService;
	@Autowired
	MessageSource messageSource;
	@Autowired
	UploadController uploadController;
	@Autowired
	HrUaService hrUaService;
	
	
	@RequestMapping(value = "/list")
	@Transactional
	public String listAll(Model model) {
		model.addAttribute("users", hrUaService.findAll());
		return "hrua/list";
	}
	
	@RequestMapping(value = "/create")
	@Transactional
	public String create(Model model, HrUaCreateForm createForm) {
		model.addAttribute("createForm", createForm);
		return "hrua/create";
	}
}