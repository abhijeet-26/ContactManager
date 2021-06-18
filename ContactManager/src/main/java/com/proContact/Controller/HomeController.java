package com.proContact.Controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proContact.Entities.User;
import com.proContact.dao.UserRepository;
import com.proContact.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title", "Home-Contact Manger");
		return "home";
	}
	
	@GetMapping("/about")
	public String abouthandler(Model model)
	{
		model.addAttribute("title", "About-Contact Manger");
		return "about";
	}
	
	
	@GetMapping("/signup")
	public String signupHandler(Model model)
	{
		model.addAttribute("title", "Register-Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String formHandler(@Valid @ModelAttribute("user")User user,BindingResult result1 ,@RequestParam(value = "agreement", defaultValue = "false")boolean agreemnet, Model model,HttpSession session)
	{
		
		model.addAttribute("title", "Register-Contact Manager");
		try {
			
			if(!agreemnet)
			{
				System.out.println("You have'nt agreed the terms and condition");
				throw new Exception("You have'nt agreed the terms and condition");
			}
			
			if(result1.hasErrors())
			{
				model.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("You have been registered successfully", "alert-success"));
			return "signup";
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went Wrong!!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
	}
	
	
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title", "Login-Contact Manager");
		return "login";
	}
	
}

