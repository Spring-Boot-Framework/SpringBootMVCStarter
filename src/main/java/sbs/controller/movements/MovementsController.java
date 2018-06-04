package sbs.controller.movements;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sbs.model.geode.GeodeMovement;
import sbs.model.x3.X3ProductFinalMachine;
import sbs.model.x3.X3ShipmentMovement;
import sbs.model.x3.X3WarehouseWeightDetailLine;
import sbs.model.x3.X3WarehouseWeightLine;
import sbs.service.dictionary.X3HistoryPriceService;
import sbs.service.geode.JdbcOracleGeodeService;
import sbs.service.x3.JdbcOracleX3Service;


@Controller
@RequestMapping("movements")
public class MovementsController {
	
	@Autowired
	MessageSource messageSource;
	@Autowired
	JdbcOracleGeodeService geodeService;
	@Autowired 
	JdbcOracleX3Service x3Service;
	@Autowired
	X3HistoryPriceService x3HistoryPriceService;  
	
	List<String> productionStores;
	List<String> receptionStores;
	List<String> shipmentStores;
	
	
    @Autowired
    public MovementsController(Environment env) {
    	productionStores = Arrays.asList(env.getRequiredProperty("warehouse.store.production").split(";"));
    	receptionStores = Arrays.asList(env.getRequiredProperty("warehouse.store.receptions").split(";"));
    	shipmentStores = Arrays.asList(env.getRequiredProperty("warehouse.store.shipments").split(";"));
    }
	
    @RequestMapping("/main")
    public String view(Model model, Locale locale){
    	MovementsForm movementsForm = new MovementsForm();
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    	cal.add(Calendar.DAY_OF_MONTH, -1);
    	movementsForm.setEndDate(new Timestamp(cal.getTimeInMillis()));
    	cal.add(Calendar.DAY_OF_MONTH, -6);
    	movementsForm.setStartDate(new Timestamp(cal.getTimeInMillis()));
    	model.addAttribute("movementsForm", movementsForm);
    	
    	return "movements/main";
    }
    
    
	@RequestMapping(value = "/calculate", params = { "shipmov" }, method = RequestMethod.POST)
	public String performSearch(@Valid MovementsForm movementsForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs, Locale locale){
		
		if(bindingResult.hasErrors()){
			return "movements/main";
		}
		
		Map<String, Double> prices = x3HistoryPriceService.findAllX3HistoryPrices();
		List<X3ShipmentMovement> movements = x3Service.findAdrShipmentMovementsInPeriod(movementsForm.getStartDate(), movementsForm.getEndDate());
		int counter = 0;
		double totalValue = 0;
		double mechanicalValue = 0;
		double weldingValue = 0;
		double assemblyValue = 0;
		double otherValue = 0;
		double warehouseValue = 0;
		LinkedHashSet<String> unassignedMachinesSet = new LinkedHashSet<>();
		String unassignedMachines = "";
		
		Map<String, X3ProductFinalMachine> machinesIndex = x3Service.findX3ProductFinalMachines("ATW");
		
		for(X3ShipmentMovement mvt: movements){
			if(prices.get(mvt.getItemCode())!=null){
				mvt.setPrice(prices.get(mvt.getItemCode()));
			}
			else{
				mvt.setPrice(mvt.getEmergencyAveragePrice());
			}
			mvt.setValue(mvt.getQuantity()*mvt.getPrice());
			if(mvt.getItemCategory().equals("ACV")){
				warehouseValue += mvt.getValue();
			}
			else if(machinesIndex.containsKey(mvt.getItemCode())){
				switch(machinesIndex.get(mvt.getItemCode()).getMachineDepartment()){
				case X3ProductFinalMachine.MECHANICAL:
					mechanicalValue += mvt.getValue();
					break;
				case X3ProductFinalMachine.WELDING:
					weldingValue += mvt.getValue();
					break;
				case X3ProductFinalMachine.ASSEMBLY:
					assemblyValue += mvt.getValue();
					break;
				default:
					otherValue += mvt.getValue();
					unassignedMachinesSet.add(machinesIndex.get(mvt.getItemCode()).getMachineCode()); 
					break;
				}
			}
			else{
				otherValue += mvt.getValue();
			}
			
			totalValue += mvt.getValue();
			counter++;
		}
		
		model.addAttribute("startDate", movementsForm.getStartDate());
		model.addAttribute("endDate", movementsForm.getEndDate());
		model.addAttribute("counter", counter);
		model.addAttribute("totalValue", totalValue);
		model.addAttribute("mechanicalValue", mechanicalValue);
		model.addAttribute("weldingValue", weldingValue);
		model.addAttribute("assemblyValue", assemblyValue);
		model.addAttribute("warehouseValue", warehouseValue);
		model.addAttribute("otherValue", otherValue);
		model.addAttribute("movements", movements);
		for (String str: unassignedMachinesSet){
			unassignedMachines += str + "; ";
		}
		model.addAttribute("unassignedMachines", unassignedMachines);
		
		return "movements/main";
	}
	
	
	@RequestMapping(value = "/calculate", params = { "rcpweightstats" }, method = RequestMethod.POST)
	public String performRcpWeightStats(@Valid MovementsForm movementsForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs, Locale locale){
		if(bindingResult.hasErrors()){
			return "movements/main";
		}
		List<X3WarehouseWeightLine> lines = x3Service.findWeightSumLine(movementsForm.getStartDate(), movementsForm.getEndDate(), JdbcOracleX3Service.WEIGHT_QUERY_RECEPTION);
		model.addAttribute("startDate", movementsForm.getStartDate());
		model.addAttribute("endDate", movementsForm.getEndDate());
		model.addAttribute("counter", lines.size());
		model.addAttribute("weight", lines);
		return "movements/main";
	}
	
	@RequestMapping(value = "/calculate", params = { "shipweightstats" }, method = RequestMethod.POST)
	public String performShipWeightStats(@Valid MovementsForm movementsForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs, Locale locale){
		if(bindingResult.hasErrors()){
			return "movements/main";
		}
		List<X3WarehouseWeightLine> lines = x3Service.findWeightSumLine(movementsForm.getStartDate(), movementsForm.getEndDate(), JdbcOracleX3Service.WEIGHT_QUERY_SHIPMENT);
		model.addAttribute("startDate", movementsForm.getStartDate());
		model.addAttribute("endDate", movementsForm.getEndDate());
		model.addAttribute("counter", lines.size());
		model.addAttribute("weight", lines);
		return "movements/main";
	}
	
	
	@RequestMapping(value = "/calculate", params = { "rcpweightdetails" }, method = RequestMethod.POST)
	public String performRcpWeightDetails(@Valid MovementsForm movementsForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs, Locale locale){
		if(bindingResult.hasErrors()){
			return "movements/main";
		}
		List<X3WarehouseWeightDetailLine> lines = x3Service.findWeightDetailLine(movementsForm.getStartDate(), movementsForm.getEndDate(), JdbcOracleX3Service.WEIGHT_QUERY_RECEPTION_DETAIL);
		model.addAttribute("startDate", movementsForm.getStartDate());
		model.addAttribute("endDate", movementsForm.getEndDate());
		model.addAttribute("counter", lines.size());
		model.addAttribute("weightdetails", lines);
		return "movements/main";
	}
	
	@RequestMapping(value = "/calculate", params = { "shipweightdetails" }, method = RequestMethod.POST)
	public String performShipWeightDetails(@Valid MovementsForm movementsForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs, Locale locale){
		if(bindingResult.hasErrors()){
			return "movements/main";
		}
		List<X3WarehouseWeightDetailLine> lines = x3Service.findWeightDetailLine(movementsForm.getStartDate(), movementsForm.getEndDate(), JdbcOracleX3Service.WEIGHT_QUERY_SHIPMENT_DETAIL);
		model.addAttribute("startDate", movementsForm.getStartDate());
		model.addAttribute("endDate", movementsForm.getEndDate());
		model.addAttribute("counter", lines.size());
		model.addAttribute("weightdetails", lines);
		return "movements/main";
	}
	
	@RequestMapping(value = "/calculate", params = { "rcpmovements" }, method = RequestMethod.POST)
	public String performRcpMovements(@Valid MovementsForm movementsForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttrs, Locale locale){
		if(bindingResult.hasErrors()){
			return "movements/main";
		}
		
		List<GeodeMovement> lines = geodeService.findMovementsInPeriod(movementsForm.getStartDate(), movementsForm.getEndDate());
		model.addAttribute("startDate", movementsForm.getStartDate());
		model.addAttribute("endDate", movementsForm.getEndDate());
		//model.addAttribute("counter", lines.size());
		//model.addAttribute("weightdetails", lines);
		return "movements/main";
	}
    
    
    
}
