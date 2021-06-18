package com.proContact.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.swing.event.TableColumnModelListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.proContact.Entities.Contact;
import com.proContact.Entities.User;
import com.proContact.dao.ContactRepository;
import com.proContact.dao.UserRepository;
import com.proContact.helper.Message;


@Controller
@RequestMapping(value="/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void commonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
	}

	@GetMapping("/index")
	public String userDashboard(Model model, Principal principal)
	{
		model.addAttribute("title", "User Dashboard");
	
		return "normal/user_dashboard";
	}
	
	@GetMapping("add_contact")
	public String addFormHandler(Model model)
	{
		model.addAttribute("title", "AddContact-Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	
	@PostMapping("/process-contact")
	public String submitFormHandler(Model model,@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session)
	{
		model.addAttribute("title", "AddContact-Contact Manager");
		try {
			String userName = principal.getName();
			
			User user = this.userRepository.getUserByUserName(userName);
			
			if(file.isEmpty())
			{
				contact.setImage("download.png");
			
			}
			else {
				//file the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile= new ClassPathResource("static/image").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			// message success
			
			session.setAttribute("message", new Message("You have successfully added data..", "success"));
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			// message errror
			
			session.setAttribute("message", new Message("Something went wrong! Try again", "danger"));
			
		}
		return "normal/add_contact_form";
	}
	
	
	// Show contacts form
	// per page=5
	@GetMapping("/show_contacts/{page}")
	public String showContactHandler(@PathVariable("page") Integer page,Model model,Principal principal)
	{
		model.addAttribute("title", "Show Contact-Contact Manager");
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 4);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	
	// show particular contact
	@GetMapping("/contact/{cid}")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model model, Principal principal)
	{
		
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		
		return "normal/contact_details";
	}
	
	
	// delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Principal principal,HttpSession session)
	{
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		
		
		if(user.getId()==contact.getUser().getId())
		{
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
		}
		
		return "redirect:/user/show_contacts/0";
	}
	
	
	// Update Form Handler
	@PostMapping("/update_contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model)
	{
		model.addAttribute("title", "Update Contact- Contact Manager");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	
	// post upadte handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,Model model,@RequestParam("profileImage") MultipartFile file,
			HttpSession session,Principal principal)
	{
		try {
			
			//old Contact details
			Contact oldDetails = this.contactRepository.findById(contact.getCid()).get();
			
			if(!file.isEmpty())
			{
				//file uploading code, rewrite
				
				//delete old photo
				File deleteFile= new ClassPathResource("static/image").getFile();
				File newFile=new File(deleteFile, oldDetails.getImage());
				newFile.delete();
				
				
				//update new photo
				File saveFile= new ClassPathResource("static/image").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}
			else {
				contact.setImage(oldDetails.getImage());
			}
			
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact Updated Successfully!", "success"));
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		return "redirect:/user/contact/"+contact.getCid();
	}
	
	
	
	// profile VIew
	@GetMapping("/profile")
	public String profileHandler(Model model)
	{
		model.addAttribute("title", "Profile Page- Contact Manager");
		return "normal/profile";
	}
	
	//setting Handler
	@GetMapping("/setting")
	public String settingHandler(Model model)
	{
		model.addAttribute("title", "Setting-Contact Manager");
		return "normal/setting";
	}
	
	@PostMapping("/change_password")
	public String changePassWordHandler(@RequestParam("oldPassWord") String oldPassWord, @RequestParam("newPassWord") String newPassWord, Principal principal,HttpSession session)
	{
		
		
		String userName = principal.getName();
		
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassWord, currentUser.getPassword()))
		{
			// change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassWord));
			this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Your Password has successfully changed!!", "success"));
		}
		else
		{
			//error
			session.setAttribute("message", new Message("Invalid password", "danger"));
			return "redirect:/user/setting";
		}
		
		return "redirect:/user/index";
	}
	
	
	
}
