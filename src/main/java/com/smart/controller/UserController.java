package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.sound.midi.Soundbank;
import javax.swing.plaf.multi.MultiButtonUI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//adding handler for current user information
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String Uname = principal.getName();
		System.out.println("user name : "+Uname);
		//getting the user name by sql query
		User user = this.userRepository.getUserByUserName(Uname);
	    System.out.println("all detail for the user : "+user);
		
	    model.addAttribute("user", user);
		 
	}
	
	@RequestMapping(value="/index",method = RequestMethod.GET)
	public String dashboard(Model model,Principal principal) {	
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	//open adding form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) {
		
		try {
			//fetching the current login information with help of principal
			String name=principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				//if file is an empty then printing the message
				System.out.println("file empty!!");
				contact.setImage("contact.jpg");
			}else {
				//uploading the file to the folder
				contact.setImage(file.getOriginalFilename());
				
				System.out.println("::::::::::: file.getOriginalFile :: "+file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				System.out.println(":::::::::: save file :::::::::::::  "+saveFile);
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				System.out.println("file path :::::::::::::::::::::::    "+path);
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			//saving the value of updated user
			
			this.userRepository.save(user);
			
			System.out.println("contact object : "+contact);
			
			// message success 
			session.setAttribute("message", new Message("Your contact is added !! Add more..","success"));
			System.out.println("session of the value ::::::::::::::::::::::::");
			
		}catch (Exception e) {
			System.out.println("Error ::::::::::: "+e.getMessage());
			e.printStackTrace();
			// message failure 
			session.setAttribute("message", new Message("Something went wrong !! Try again","danger"));
		}
		System.out.println("===================================================");
		return "normal/add_contact_form";
	}
	
	
	
	// showing contacts handler
	//per page = 5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		m.addAttribute("title","Show User Contacts");
		//sending the list of all users
		/*
		 * String userName= principal.getName(); User user =
		 * this.userRepository.getUserByUserName(userName); List<Contact> contacts =
		 * user.getContacts();
		 */
		
		/* need to find current user */
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//storing the pageble information
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		  
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal) {
		System.out.println("getting the particulat cId : "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//solving security bug problem
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	//deleting particular handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession session,
			Principal principal) {
		System.out.println("------------------------");
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//check....
//		if(contact.getUser().getId()==cId)
		System.out.println("checking the value of userId and getId : "+contact.getUser().getContacts()+" - "+cId);
//		// because, here using cascade type connectivity in User and Contact table
//		// show, for deleting case, will destroy the contact connection with user table then delete the contact
//		// from the contact table
//		contact.setUser(null);
//		//removing image
////		this.contactRepository.delete(contact);
//	
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		System.out.println("deleted successfully!!!!");
		session.setAttribute("message", new Message("Contact deleted successfully","success"));
		return "redirect:/user/show-contacts/0";
	}
	
	
	//open update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cid,Model m) {
		System.out.println("------------ we are in the update form section---------"+cid);
		m.addAttribute("title","Updating Contact");
		Optional<Contact> contact = this.contactRepository.findById(cid);
		Contact contact2 = contact.get();
		m.addAttribute("contact", contact2);

		System.out.println("------------ we are in the update form section---------"+contact2.getcId());
		return "normal/update_form";
	}
	
	//updating the contact handler on the basis of database
	@PostMapping("/process-update/{cId}")
	public String updateHandler(@PathVariable("cId") Integer cid,@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,Principal principal) {
	
		
		try {
			System.out.println(contact.getName()+" "+contact.getUser()+"  "+cid);
			//old contact
			Contact oldcontactDetail = this.contactRepository.findById(cid).get();
			
			
			//image changes checking
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				//deleting old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetail.getImage());
				file1.delete();
				//updating new photo 
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(oldcontactDetail.getImage());    
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact Upadated..","success"));
			
		}catch(Exception e) {
			System.out.println("------------------exception part  "+contact.getcId());
			e.printStackTrace();
			
		}
		/* /user/28/contact */
		            
		return "redirect:/user/"+contact.getcId()+"/contact"; 
	}
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "Profile Page");
		return "normal/profile";
	}

}
