package sbs.controller.cebs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javassist.NotFoundException;
import sbs.helpers.DateHelper;
import sbs.model.users.User;
import sbs.service.mail.MailService;
import sbs.service.users.UserService;

@Controller
@RequestMapping("cebs")
public class CebsController {

	@Autowired
	DateHelper dateHelper;
	@Autowired
	UserService userService;
	@Autowired
	MailService mailService;
	@Autowired
	TemplateEngine templateEngine;

	private boolean active;
	private boolean confirmed;
	private boolean sent;
	private Map<Long, CebsItem> items;
	private String organizer;
	private Calendar actionDate;
	private String dayCode;
	private String location;
	private String locationCode;

	@ModelAttribute
	public void addAttributes(Model model) {
		model.addAttribute("organizer", organizer);
		model.addAttribute("active", active);
		model.addAttribute("confirmed", confirmed);
		model.addAttribute("sent", sent);
		model.addAttribute("actionDate", dateHelper.formatDdMmYyyy(actionDate.getTime()));
		model.addAttribute("dayCode", dayCode);
		model.addAttribute("location", this.location);
	}

	public CebsController() {
		active = false;
		confirmed = false;
		sent = false;
		items = new TreeMap<>();
		actionDate = Calendar.getInstance();
	}

	@RequestMapping("/order")
	public String order(Model model) {

		prepareOrderView(model);
		model.addAttribute(new CebsOrderForm());

		return "cebs/order";
	}

	private void prepareOrderView(Model model) {
		if (active) {
			User currentUser = userService.getAuthenticatedUser();
			List<CebsItem> myItems = new ArrayList<>();
			Double amount = 0.0;
			for (CebsItem item : this.items.values()) {
				if (item.getUser().getId().equals(currentUser.getId())) {
					myItems.add(item);
					amount += item.getAmount();
				}
			}

			model.addAttribute("menu", getMenuList());
			model.addAttribute("items", myItems);
			model.addAttribute("amount", amount);
		}
		model.addAttribute("active", active);
		model.addAttribute("confirmed", confirmed);
		model.addAttribute("sent", sent);
	}

	private List<MenuItem> getMenuList() {
		List<MenuItem> list = new ArrayList<>();
		try {
			String file = "";
			switch(this.locationCode) {
				case "kml":
					file = "data/cebs_KM.dat";
				break;
				case "bks":
					file = "data/cebs_BK.dat";
				break;
			}
			
			File resource = new ClassPathResource(file).getFile();

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(resource), "UTF8"));

			String st;
			MenuItem mi;
			String[] line;
			while ((st = br.readLine()) != null) {
				mi = new MenuItem();
				line = st.split(";");
				mi.setId(line[0]);
				mi.setText(line[1]);
				mi.setPrice(Double.parseDouble(line[2]));
				list.add(mi);
			}
			br.close();

		} catch (Exception ex) {
		}
		return list;
	}

	@RequestMapping("/order/delete/{itemId}")
	public String deleteQuestion(@PathVariable("itemId") long itemId, RedirectAttributes redirectAttrs) {
		this.items.remove(itemId);
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/paid/{itemId}")
	public String itempaidSwitch(@PathVariable("itemId") long itemId, RedirectAttributes redirectAttrs) {
		items.get(itemId).setPaid(!items.get(itemId).isPaid());
		return "redirect:/cebs/manage";
	}

	@RequestMapping("/manage/confirmed")
	public String confirmedSwitch(RedirectAttributes redirectAttrs) throws UnknownHostException, MessagingException {
		this.confirmed = !this.confirmed;
		if (confirmed) {
			String title = "Zamówienie potwierdzone";
			String message = "Zamówienie zostało zaakceptowane przez bar i na pewno zostanie złożone. Prosimy o zapłatę.";
			this.sendMail(title, message, true);
		} else {
			String title = "Potwierdzenie ODWOŁANE";
			String message = "Potwierdzenie zostało ODWOŁANE. Nie wiemy, czy zostanie zrealizowane - wstrzymujemy zbiórkę pieniędzy.";
			this.sendMail(title, message, true);
		}
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/sent")
	public String sentSwitch(RedirectAttributes redirectAttrs) throws UnknownHostException, MessagingException {
		this.sent = !this.sent;
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/arrived")
	public String arrived(RedirectAttributes redirectAttrs) throws UnknownHostException, MessagingException {

		String title = "Zamówienie gotowe do odbioru!";
		String message = "Przyjechała dostawa. Zapraszamy po odbiór ;-)";
		
		new Timer().schedule(
			    new TimerTask() {
			        @Override
			        public void run() {
			        	try {
							sendMail(title, message, true);
						} catch (UnknownHostException | MessagingException e) {
							
						}
			        }
			    }, 
			    180000
			);

		return "redirect:/cebs/order";
	}

	@RequestMapping(value = "/order/add", params = { "add" }, method = RequestMethod.POST)
	public String addToOrder(@RequestParam String add, CebsOrderForm cebsOrderFom,
			RedirectAttributes redirectAttrs, Locale locale, Model model) throws NotFoundException {
		if (
				!sent || 
				userService.getAuthenticatedUser().hasRole("ROLE_CEBSMANAGER") || 
				userService.getAuthenticatedUser().hasRole("ROLE_ADMIN")
			) {
			List<MenuItem> menu = getMenuList();
			for (MenuItem item : menu) {
				if (item.getId().equals(add)) {
					CebsItem ci = new CebsItem(userService.getAuthenticatedUser());
					ci.setComment(cebsOrderFom.getComment().trim());
					ci.setItem(item.getText());
					ci.setQuantity(cebsOrderFom.getQuantity());
					ci.setAmount(item.getPrice() * cebsOrderFom.getQuantity());
					this.items.put(ci.getId(), ci);
				}
			}
		}
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/starttoday/{loc}")
	public String startToday(@PathVariable("loc") String loc, Model model) throws Exception {
		this.locationCode = loc;
		switch(loc) {
			case "kml":
				this.location = "KebabMania LESKO";
			break;
			case "bks":
				this.location = "Bar Kebab SANOK";
			break;
			default:
				throw new Exception("Unrecognized location " + loc);
		}
		this.active = true;
		this.confirmed = false;
		this.sent = false;
		this.actionDate = Calendar.getInstance();
		this.dayCode = "general.calendar.day" + actionDate.get(Calendar.DAY_OF_WEEK);
		this.organizer = userService.getAuthenticatedUser().getName();
		String title = "Zbieramy na DZISIAJ w " + this.location + "!";
		String message = "Jeżeli wszystko się uda, zamówienie będzie relizowane DZISIAJ, "
				+ dateHelper.formatDdMmYyyy(actionDate.getTime()) + " w " + this.location
				+ ". <br/>Proszę wejść na stronę z linku poniżej i dopisać się do listy ;)";
		this.sendMail(title, message, false);
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/starttomorrow/{loc}")
	public String startTomorrow(@PathVariable("loc") String loc, Model model) throws Exception {
		this.locationCode = loc;
		switch(loc) {
			case "kml":
				this.location = "KebabMania LESKO";
			break;
			case "bks":
				this.location = "Bar Kebab SANOK";
			break;
			default:
				throw new Exception("Unrecognized location " + loc);			
		}
		this.active = true;
		this.confirmed = false;
		this.sent = false;
		this.actionDate = Calendar.getInstance();
		this.actionDate.add(Calendar.DAY_OF_MONTH, 1);
		this.dayCode = "general.calendar.day" + actionDate.get(Calendar.DAY_OF_WEEK);
		this.organizer = userService.getAuthenticatedUser().getName();
		String title = "Zbieramy na JUTRO w " + this.location + "!";
		String message = "Jeżeli wszystko się uda, zamówienie będzie relizowane JUTRO, "
				+ dateHelper.formatDdMmYyyy(actionDate.getTime()) + " w " + this.location
				+ ". <br/>Proszę wejść na stronę z linku poniżej i dopisać się do listy ;)";
		this.sendMail(title, message, false);
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/cancel")
	public String cancel(Model model) throws UnknownHostException, MessagingException {
		this.active = false;
		this.confirmed = false;
		this.sent = false;
		String title = "Anulujemy akcję zamawiania";
		String message = "Anulujemy akcję zamawiania, bar nieczynny lub inny powód - może innym razem";
		this.sendMail(title, message, true);
		this.items.clear();
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage/finish")
	public String finish(Model model) throws UnknownHostException, MessagingException {
		this.active = false;
		this.confirmed = false;
		this.sent = false;
		this.items.clear();
		return "redirect:/cebs/order";
	}

	@RequestMapping("/manage")
	public String manage(Model model) {
		Double amount = 0.0;
		Double paid = 0.0;
		List<CebsItem> currentItems = new ArrayList<>();
		Map<String, Integer> summary = new TreeMap<>();

		for (CebsItem item : items.values()) {
			currentItems.add(item);
			amount += item.getAmount();
			if(item.isPaid()){
				paid += item.getAmount();
			}
			summary.put(item.getItem(), item.getQuantity() + summary.getOrDefault(item.getItem(), 0));
		}

		model.addAttribute("items", currentItems);
		model.addAttribute("amount", amount);
		model.addAttribute("paid", paid);
		model.addAttribute("missing", amount-paid);
		model.addAttribute("summary", summary);

		return "cebs/manage";
	}

	private void sendMail(String title, String message, boolean orderedOnly) throws UnknownHostException, MessagingException {

		List<User> cebsUsers = new ArrayList<>();
		
		if(orderedOnly){
			cebsUsers = getOrderedMailingList();
		}
		else{
			cebsUsers = userService
					.findByAnyRole(new String[] { "ROLE_CEBSMANAGER", "ROLE_CEBSUSER", "ROLE_ADMIN" });	
		}
		
		ArrayList<String> mailTo = new ArrayList<>();
		for (User user : cebsUsers) {
			mailTo.add(user.getEmail());
		}

		Context context = new Context();
		context.setVariable("host", InetAddress.getLocalHost().getHostAddress());
		context.setVariable("title", title);
		context.setVariable("message", message);
		String body = templateEngine.process("cebs/mailtemplate", context);
		if(mailTo.size()>0){
			mailService.sendEmail("webapp@atwsystem.pl", mailTo.toArray(new String[0]), new String[0], title, body);
		}
	}

	private List<User> getOrderedMailingList() {
		List<User> list = new ArrayList<>();
		for (CebsItem item : items.values()) {
			list.add(item.getUser());
		}
		return list;
	}

}
