package au.com.digitalspider.cube.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	private final Logger LOG = Logger.getLogger(IndexController.class);

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/cube")
	public String cube() {
		return "index";
	}
}