package com.smart.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@GetMapping("/test")
	@ResponseBody
	public String test() {
		User user = new User();
		user.setName("Saksham");
		user.setEmail("saks123ham@gmail.com");
		User saveN = this.userRepository.save(user);
		System.out.println("wanna to see the data :::  "+saveN);
		return "working";
	}
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager ");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager ");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "Signup - Smart Contact Manager ");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//handler for the registration form
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value="agreement",defaultValue = "false") boolean agreement, 
			Model model,HttpSession session) {
		try {
			
			System.out.println("------------||||------ value of result : "+result1);
			if(!agreement) {
				System.out.println("You have not agreed with terms and conditions!");
				throw new Exception("You have not agreed with terms and conditions!");
			}
			if(result1.hasErrors()) {
				System.out.println("error in the validation part : "+result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			
			//setting the other role of the user table attribute
			user.setRole("ROLE_USER");
			user.setEnable(true);
			user.setImageUrl("default.png");
			
			//using encoding on the user password
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("Agreement  "+agreement);
			System.out.println("User "+user);
			User saveR = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			System.out.println("result : "+saveR);
			session.setAttribute("message", new Message("Successfully Registered","alert-success"));
			return "signup";
			
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("we are in the catch block of the do_registration POST request!! ");
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something is wrong !! "+e.getMessage(), "alert-danger"));
			e.printStackTrace();
			return "signup";
		}
	
	}
	
	
	
	//handler for the custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manager ");
		System.out.println("------------------------------------ "+ model);
		return "login";
	}
	
	
	
	//404 page not available handle
	
	@GetMapping("/failure")
	public String failure(Model model) {
		model.addAttribute("title", "404 page not found");
		return "failure";
	}

}
