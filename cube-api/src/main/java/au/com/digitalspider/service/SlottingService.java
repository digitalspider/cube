package au.com.digitalspider.service;

import java.util.List;

import au.com.digitalspider.bean.StockBinBean;
import au.com.digitalspider.constant.BinType;
import au.com.digitalspider.constant.StockType;
import au.com.digitalspider.enums.ZoneType;

public interface SlottingService {

	/**
	 * Find a random smallest slot in the warehouse, which can take the calculated available quantity of a product.
	 * Calculation of the quantity is done by the maximum of minimumStock and
	 * {@link StockService#calculateStockAvailable(au.com.booktopia.ez.beans.ProductBean, au.com.booktopia.ez.constant.StockType)}
	 *
	 * @param productUidOrIsbn
	 * @return
	 * @throws Exception
	 */
	public StockBinBean findProductSlot(String productUidOrIsbn) throws Exception;

	/**
	 * Find a random smallest slot in the warehouse, which can take the given quantity of a product.
	 * onlyMultiBins is false.
	 * {@link ZoneType} is "S" = Stock Picking zones.
	 * {@link StockType} is WEBSITE. Can be changed by setting this using {@link #setStockType(StockType)}
	 *
	 * @param productUidOrIsbn
	 * @param quantity
	 * @return
	 * @throws Exception
	 */
	public StockBinBean findProductSlot(String productUidOrIsbn, int quantity) throws Exception;

	/**
	 * Find a random smallest slot in the warehouse, which can take the given quantity of a product.
	 *
	 * @param productUidOrIsbn
	 * @param quantity
	 * @param zones
	 * @param binTypes
	 * @param stockType
	 * @param onlyMultiBins
	 * @return
	 * @throws Exception
	 */
	public StockBinBean findProductSlot(String productUidOrIsbn, int quantity, List<ZoneType> zones, List<BinType> binTypes, StockType stockType, boolean onlyMultiBins) throws Exception;

	public StockType getStockType();

	public void setStockType(StockType stockType);

}
