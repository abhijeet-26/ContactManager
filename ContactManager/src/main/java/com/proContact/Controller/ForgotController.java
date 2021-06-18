package com.proContact.Controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proContact.Entities.User;
import com.proContact.Service.EmailService;
import com.proContact.dao.UserRepository;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/forgot")
	public String forgotController(Model model)
	{
		model.addAttribute("title", "Forgot PassWord- Contact Manager");
		return "forgot_password";
		
	}
	
	@PostMapping("/send-otp")
	public String optValidation(@RequestParam("email") String email,HttpSession session)
	{
		System.out.println(email);
		
		// Generating OTP
		int otp = random.nextInt(9999);
		System.out.println("OTP "+otp);
		
		//Code for sending otp to gmail
		
		String subject="OTP- Contact Manager";
		String message=""
				+"<div style='border: 1px solid #e2e2e2; padding:20px;'>"
				+"<h1>"
				+"OTP is "
				+"<b>"+ otp
				+"</b>"
				+"</h1>"
				+"</div>";
				
		String to=email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "varify_otp";
		}
		else {
			session.setAttribute("message", "Invalid Email");
			
			return "forgot_password";
		}
		
		
	}
	
	@PostMapping("/varify_otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session)
	{
		int myOtp=(int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		
		if(myOtp==otp)
		{
			User user = this.userRepository.getUserByUserName(email);
			if(user==null)
			{
				//send error message
				session.setAttribute("message", "User does not exists");
				
				return "forgot_password";
				
			}
			
			else {
				
			}
			
			return "change_password";
		}
		else {
			session.setAttribute("message", "OTP MisMatched");
			return "varify_otp";
		}
		
	}
	
	// change password
	@PostMapping("/submit_password")
	public String chnagePasswordHandler(@RequestParam("newPassword") String newPassword,HttpSession session)
	{
		String email=(String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bcryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		
		
		return "redirect:/signin?change=password Changed successfully";
	}
	
	
	
	
}
