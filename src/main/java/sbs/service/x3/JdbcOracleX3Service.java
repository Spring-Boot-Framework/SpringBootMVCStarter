package sbs.service.x3;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sbs.controller.dirrcpship.DirectReceptionsShipmentLine;
import sbs.model.proprog.Project;
import sbs.model.x3.*;

public interface JdbcOracleX3Service {
	
	public static final int WEIGHT_QUERY_RECEPTION = 1;
	public static final int WEIGHT_QUERY_RECEPTION_DETAIL = 2;
	public static final int WEIGHT_QUERY_SHIPMENT = 3;
	public static final int WEIGHT_QUERY_SHIPMENT_DETAIL = 4;
	
	public static final String LANG_POLISH = "POL";
	public static final String LANG_ENGLISH = "ENG";
	public static final String LANG_ITALIAN = "ITA";
	
	public String convertLocaleToX3Lang(Locale locale);
    public List<String> findAllUsers(String company);
    public List<X3Product> findAllActiveProducts(String company);
	public X3Product findProductByCode(String company, String code);
    public String findItemDescription(String company, String code);
	public List<X3Client> findAllClients(String company);
	public X3Client findClientByCode(String company, String code);
	public List<X3SalesOrder> findAllSalesOrders(String company);
	public List<X3SalesOrder> findOpenedSalesOrders(String company);
	public X3SalesOrder findSalesOrderByNumber(String company, String number);
	public X3PurchaseOrder findPurchaseOrderByNumber(String company, String number);
	public X3ProductionOrderDetails getProductionOrderInfoByNumber(String company, String number);
	public List<X3BomItem> findBomPartsByParent(String company, String productCode);
	public Object findLocationsOfProduct(String company, String code);
	public List<X3BomItem> findProductionPartsByProductionOrderAndOperation(String company, String productionOrder, int operationNumber);
	public String findOperationDescriptionByProductionOrder(String company, String order, int operation);
	public String findFinalClientByOrder(String company, String order);
	public List<X3ShipmentMovement> findAdrShipmentMovementsInPeriod(Date startDate, Date endDate);
	public Map<String, X3UtrMachine> findAllUtrMachines(String company);
	public Map<String, X3UtrWorker> findAllUtrWorkers(String company);
	public Map<String, X3UtrFault> findUtrFaultsInPeriod(Date startDate, Date endDate);
	public Map<String, X3UtrFault> findAllUtrFaults();
	public List<X3UtrFaultLine> findUtrFaultLinesAfterDate(Date startDate);
	public List<X3UtrFaultLine> findAllUtrFaultLines();
	public Map<String, X3ProductFinalMachine> findX3ProductFinalMachines(String company);
	public List<Project> findPendingProjectsProgress();
	public Project findProjectProgressByNumber(String number);
	public List<X3WarehouseWeightLine> findWeightSumLine(Date startDate, Date endDate, int weightQueryType);
	public List<X3WarehouseWeightDetailLine> findWeightDetailLine(Date startDate, Date endDate, int weightQueryReceptionDetail);
	public Map<String, X3ProductSellDemand> findAcvProductSellDemand(Date startDate, Date endDate, String company);
	public List<String> findAcvNonProductionCodes(String company);
	public Map<String, Integer> findAcvMagStock(String company);
	public Map<String, Integer> findAcvAverageUsageInPeriod(String startPeriod, String endPeriod, String company);
	public Map<String, Integer> findAcvShipStock(String company);
	public List<X3ShipmentStockLineWithPrice> findAllShipStockWithAveragePrice(String company);
	public List<DirectReceptionsShipmentLine> findDirectReceptionsShipmentLines(Date startDate, Date endDate, String company);
	public Map<String, Integer> getAcvConsumptionListForYear(int year, String company);
	public Map<String, Integer> getAcvDemandList(String company);
	public Map<String, X3ConsumptionSupplyInfo> getAcvListOfLastSupplyInfo(String company);
	public List<X3ConsumptionProductInfo> getAcvListForConsumptionReport(String company);
	public Map<String, String> getAcvProductsEnglishDescriptions(String company);
	public List<X3SalesOrderLine> findOpenedSalesOrderLinesInPeriod(Date startDate, Date endDate, String company);
	public Map<String, Integer> findGeneralStockForAllProducts(String company);
	public X3Workstation findWorkstationByCode(String company, String code);
	public boolean checkIfLocationExist(String company, String location);
	public String findPackageDescription(String company, String packageCode);
	public Map<String, String> getDescriptionsByLanguage(String x3lang, String company);
	public List<X3UsageDetail> getAcvUsageDetailsListByYear(int year, String company);
	public List<X3CoverageData> getCoverageInitialData(String company);
	public Map<String, X3Supplier> getFirstAcvSuppliers(String company);
	public List<X3SalesOrderItemSum> findAllSalesOrdersItemsInPeriod(Date startDate, Date endDate, String company);
}
