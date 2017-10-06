package au.com.digitalspider.controller;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.com.digitalspider.bean.StockBinBean;
import au.com.digitalspider.exception.ApplicationException;
import au.com.digitalspider.factory.ServiceFactory;
import au.com.digitalspider.service.SlottingService;
import au.com.digitalspider.model.dto.SlottingResult;

/**
 *	Services to screens:
 *	<br/>Home -> Login
 *
 */
@Path("slot")
@Component
@Produces({ MediaType.APPLICATION_JSON })
@Scope("singleton")
public class SlottingResource extends BaseResource{

	private static Logger LOG = Logger.getLogger(SlottingResource.class);

	/**
	 * Determines the best slot for a given product.
	 *
	 * Information is provided by url parameters in the format: /get/{productId}{path:(/.*)?}
	 * Example:
	 * <ul>
	 * 	<li>http://localhost:8080/ezwebservice/ezw/slot/1234567   # Auto calculate the quantity</li>
	 *  <li>http://localhost:8080/ezwebservice/ezw/slot/1234567/qty/12 # Manually set the quantity to 12</li>
	 * </ul>
	 * The work is then done using {@link SlottingService#findProductSlot(String)}
	 *
	 * @param productId the productId or isbn to find a slot for
	 * @param path potentially an additional quantity value
	 * @return The {@link SlottingResult} as a json response with success, and a message. Format is {"success":true,"message":"S4C-12-B02"}
	 * @throws ApplicationException
	 */
	@GET
	@Path("/{productId}{path:(/.*)?}")
	@Produces("application/json")
	public Response getSlot(
			@PathParam(value = "productId") String productId,
			@PathParam(value = "path") String path)

			throws ApplicationException {

		LOG.info(MessageFormat.format("Test cubing with productId={0}, path={1}", productId,path));

		//response DTO
		SlottingResult slottingResult = new SlottingResult();
		boolean success = false;

		try {
			SlottingService slottingService = ServiceFactory.getInstance().getSlottingService();

			// Validate parameters not null or ""
			handleEmptyError(productId, "Product");

			try {
				Map<String, String> params = parsePath(path);
				int quantity = 0;
				if (!params.isEmpty()) {
					String quantityString = params.get("qty");
					if (StringUtils.isNotBlank(quantityString) && StringUtils.isNumeric(quantityString)) {
						quantity = Integer.parseInt(quantityString);
					}
				}
				LOG.info("quantity="+quantity);


				LOG.info("slottingService.findProductSlot() START");
				StockBinBean stockBin = null;
				if (quantity>0) {
					stockBin = slottingService.findProductSlot(productId, quantity);
				} else {
					stockBin = slottingService.findProductSlot(productId);
				}
				LOG.info("slottingService.findProductSlot() DONE. stockBin="+stockBin);
				success = true;
				slottingResult.setMessage(stockBin.toString());
			} catch (Exception e) {
				LOG.error(e);
				slottingResult.setMessage(e.getMessage());
			}

			//process result
			slottingResult.setSuccess(success);

		} catch (Exception e) {
			handleFatalError(e);
		}

		return buildOkResponse(slottingResult);
	}

	/**
	 * Take a string "/key/123/key2/456" and return it as a map
	 *
	 * @param path
	 * @return
	 */
	Map<String, String> parsePath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		String[] pathParts = path.split("/");
		Map<String, String> pathMap = new HashMap<String, String>();
		for (int i = 0; i < pathParts.length / 2; i++) {
			String key = pathParts[2 * i];
			String value = pathParts[2 * i + 1];
			pathMap.put(key, value);
		}
		return pathMap;
	}
}
