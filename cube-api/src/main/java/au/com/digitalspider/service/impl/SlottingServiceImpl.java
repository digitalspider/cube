package au.com.digitalspider.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import au.com.digitalspider.enums.ZoneType;
import au.com.digitalspider.bean.BinDimensionBean;
import au.com.digitalspider.bean.CubeItem;
import au.com.digitalspider.bean.ProductBean;
import au.com.digitalspider.bean.ProductDimensionsBean;
import au.com.digitalspider.bean.StockBean;
import au.com.digitalspider.bean.StockBinBean;
import au.com.digitalspider.constant.BinType;
import au.com.digitalspider.constant.StockType;
import au.com.digitalspider.service.BinDimensionService;
import au.com.digitalspider.service.CubingService;
import au.com.digitalspider.service.ProductDimensionsService;
import au.com.digitalspider.service.ProductService;
import au.com.digitalspider.service.SlottingService;
import au.com.digitalspider.service.StockBinService;
import au.com.digitalspider.service.StockService;

public class SlottingServiceImpl implements SlottingService {

	public static Logger LOG = Logger.getLogger(SlottingServiceImpl.class);

	private StockService stockService;
	private CubingService cubingService;
	private ProductService productService;
	private ProductDimensionsService productDimensionsService;
	private StockBinService stockBinService;
	private BinDimensionService binDimensionService;
	private StockType stockType;

	public SlottingServiceImpl(StockService stockService, CubingService cubingService, ProductService productService, ProductDimensionsService productDimensionsService, StockBinService stockBinService, BinDimensionService binDimensionService) {
		this.stockService = stockService;
		this.cubingService = cubingService;
		this.productService = productService;
		this.productDimensionsService = productDimensionsService;
		this.stockBinService = stockBinService;
		this.binDimensionService = binDimensionService;
		this.stockType = StockType.WEBSITE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StockBinBean findProductSlot(String productUidOrIsbn) throws Exception {
		ProductBean productBean = getProductBean(productUidOrIsbn);
		LOG.info("productBean="+productBean);
		if (productBean==null) {
			throw new Exception("Cannot find product for uid or isbn = "+productUidOrIsbn);
		}
		int quantity = getStockQuantityToSlot(productBean, stockType);

		return findProductSlot(productUidOrIsbn, quantity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StockBinBean findProductSlot(String productUidOrIsbn, int quantity) throws Exception {
		boolean onlyMultiBins = false;
		List<ZoneType> zones = ZoneType.getStockPickingZones();
		List<BinType> binTypes = new ArrayList<BinType>();
		binTypes.add(BinType.STOCK_PICKING);

		return findProductSlot(productUidOrIsbn, quantity, zones, binTypes, stockType, onlyMultiBins);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StockBinBean findProductSlot(String productUidOrIsbn, int quantity, List<ZoneType> zones, List<BinType> binTypes, StockType stockType, boolean onlyMultiBins) throws Exception {
		LOG.info("slotProduct START. productUid="+productUidOrIsbn);
		LOG.debug("stockService="+stockService);
		LOG.debug("cubingService="+cubingService);
		LOG.debug("productService="+productService);
		LOG.debug("productDimensionsService="+productDimensionsService);
		LOG.debug("stockBinService="+stockBinService);

		// Get the product bean
		ProductBean productBean = getProductBean(productUidOrIsbn);
		LOG.info("productBean="+productBean);
		if (productBean==null) {
			throw new Exception("Cannot find product for uid or isbn = "+productUidOrIsbn);
		}
		long productUid = productBean.getId();

		// Get the product dimensions
		ProductDimensionsBean productDimensionsBean = productDimensionsService.getProductDimensions(productUid);
		LOG.info("productDimensionsBean="+productDimensionsBean);
		if (productDimensionsBean==null) {
			throw new Exception("Cannot find product dimensions for productUid="+productUid);
		}

		// Get the bin dimension types
		String row = "ALL";
		List<BinDimensionBean> binDimensionBeanList = binDimensionService.getAvailableBinDimensions(zones, binTypes, row);
		LOG.info("binDimensionBeanList="+binDimensionBeanList);
		if (binDimensionBeanList==null || binDimensionBeanList.isEmpty()) {
			throw new Exception("The are no available bins in the warehouse for productUid="+productUid);
		}

		// Map product dimensions and quantity to cubeItem
		CubeItem cubeItem = new CubeItem(""+productUid);
		// Ensure that the shortest side is always the length
		if (productDimensionsBean.getLength()>productDimensionsBean.getWidth()) {
			cubeItem.length = productDimensionsBean.getWidth();
			cubeItem.width = productDimensionsBean.getLength();
		} else {
			cubeItem.length = productDimensionsBean.getLength();
			cubeItem.width = productDimensionsBean.getWidth();
		}
		cubeItem.height = productDimensionsBean.getHeight();
		cubeItem.weight = productDimensionsBean.getWeight();
		cubeItem.quantity = quantity;
		List<CubeItem> cubeList = new ArrayList<CubeItem>();
		cubeList.add(cubeItem);

		// Find the optimal bin dimension type by sorting the types, and finding the first type available using cubingService.calculateCubeSpace()
		binDimensionService.sortForSlotting(binDimensionBeanList);
		BinDimensionBean optimalBinDimensionBean = null;
		for (BinDimensionBean binDimensionBean : binDimensionBeanList) {
			try {
				// Note: This maps book length to shelf width, and book width to shelf depth
				cubingService.calculateCubeSpace(cubeList, binDimensionBean.getWidth(), binDimensionBean.getDepth(), binDimensionBean.getHeight(), binDimensionBean.getWeight());
				optimalBinDimensionBean = binDimensionBean;
				break;
			} catch (Exception e) {
				LOG.warn("Product ["+productUid+"] title="+productBean.getTitle()+" qty="+quantity+" done not fit into "+binDimensionBean+". ERROR: " +e.getMessage());
			}
		}
		if (optimalBinDimensionBean==null) {
			throw new Exception("Product ["+productUid+"] title="+productBean.getTitle()+" qty="+quantity+" done not fit into any of the warehouse stock zones available!");
		}
		LOG.info("Found optimal bin dimension type = "+optimalBinDimensionBean);

		// Find stock bins available for the optimal bin type
		List<StockBinBean> stockBinBeanList = stockBinService.getAvailableBins(zones, binTypes, row, onlyMultiBins, optimalBinDimensionBean.getId());
		LOG.info("stockBinBeanList="+stockBinBeanList);
		if (stockBinBeanList==null || stockBinBeanList.isEmpty()) {
			throw new Exception("Could not find an available bin in the warehouse for productUid="+productUid+" of dimension="+optimalBinDimensionBean);
		}

		// Find a random stock bin in the list of available stock bins
		StockBinBean result = null;
		int index = new Random().nextInt(stockBinBeanList.size());
		result = stockBinBeanList.get(index);

		LOG.info("slotProduct DONE. productUid="+productUid+" result="+result);
		return result;
	}

	/**
	 * Get a product bean given either a UIDPK or a Code/ISBN
	 *
	 * @param productUidOrIsbn
	 * @return
	 * @throws Exception
	 */
	private ProductBean getProductBean(String productUidOrIsbn) throws Exception {
		ProductBean productBean = productService.getProduct(productUidOrIsbn);
		if (productBean==null && StringUtils.isNumeric(productUidOrIsbn)) {
			productBean = productService.getProduct(Long.parseLong(productUidOrIsbn)); // Search by Product UIDPK
		}
		return productBean;
	}

	/**
	 * Return the available stock, no lower than the minimum stock quantity.
	 *
	 * Uses {@link StockService#getStock(StockType)} and {@link StockService#calculateStockAvailable(ProductBean, StockType, StockBean)}
	 *
	 * @param productBean
	 * @return
	 * @throws SQLException
	 */
	private int getStockQuantityToSlot(ProductBean productBean, StockType stockType) throws SQLException {
		int quantity = 0;

		StockBean stockBean = stockService.getStock(productBean, stockType, false);
		int minStock = stockBean.getMinimumStock();
		int available = stockService.calculateStockAvailable(productBean,stockType, stockBean);

		if (available<minStock) {
			quantity = minStock;
		} else {
			quantity = available;
		}
		return quantity;
	}

	@Override
	public StockType getStockType() {
		return stockType;
	}

	@Override
	public void setStockType(StockType stockType) {
		this.stockType = stockType;
	}

}
