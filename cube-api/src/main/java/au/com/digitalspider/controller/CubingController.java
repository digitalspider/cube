package au.com.digitalspider.controller;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.omg.CORBA.portable.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

import au.com.digitalspider.bean.CubeItem;
import au.com.digitalspider.bean.CubeSpace;
import au.com.digitalspider.bean.CubingInputBean;
import au.com.digitalspider.bean.Orientation;
import au.com.digitalspider.service.CubingService;

@RequestMapping("/cube")
@Component
public class CubingController {

	private static Logger LOG = Logger.getLogger(CubingController.class);

	@Autowired
	private CubingService cubingService;

	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	/**
	 * Determines if products fit in the dimensions given.
	 *
	 * Information is provided by url parameters in the format: /{maxLength}/{maxWidth}/{maxHeight}/{maxWeight}/{productId1}/{qty1}{path:(/.*)?}
	 * Example:
	 * <ul>
	 * 	<li>http://localhost:8080/ezwebservice/ezw/cube/25/25/3.5/7.0/1234567/2/943021231/3</li>
	 * </ul>
	 * The above example processes:
	 * <ul>
	 * 	<li>constraints: maxLength=25, maxWidth=25, maxHeight=3.5 and weight=7.0</li>
	 *  <li>products: 1234567 x2 and 943021231 x3</li>
	 * </ul>
	 * The dimensions of the products are looked up using {@link #populateCubeItemDimensions(List)}.
	 * They are converted into a list of {@link CubeItems} and processed using {@link CubingService#calculateCubeSpace}
	 *
	 * @param constraints json input with format {weight:0.281,length:17.8,width:11.1,height:3.2}
	 * @param cubes json input with format [{id:25845880,weight:0.500,length:24.4,width:16.8,height:2.0,quantity:1},{id:29854048,weight:0.028,length:22.9,width:15.2,height:2.5,quantity:3}]
	 * @return The {@link CubingResult} as a json response with success, and a message. Format is {"success":true,"message":"CubeSpace[0.0,0.0,0.0] spaces=132 cubes=2 l=284.90/300.0, w=299.20/300.0 h=16.20/30.0 volume=45.20%"}
	 * @throws ApplicationException
	 */
	@RequestMapping(value = "/{maxLength}/{maxWidth}/{maxHeight}/{maxWeight}/{productId1}/{qty1}{path:(/.*)?}",
			method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<?> startContainer(HttpServletRequest request, @PathVariable String maxLength,
			@PathVariable String maxWidth, @PathVariable String maxHeight,
			@PathVariable String maxWeight, @PathVariable String productId1, @PathVariable String qty1) {
		AjaxResponseBody<CubeSpace> result = new AjaxResponseBody<>();

		LOG.info(MessageFormat.format("Test cubing with length={0},width={1},height={2},weight={3}, and productId1={4},qty1={5}, path={6}", maxLength,maxWidth,maxHeight,maxWeight,productId1,qty1,path));
		boolean success = false;

//		Map<String, String> params = parsePath(path);
//		for (String key : params.keySet()) {
//			try {
//				productIdsList.add(key);
//			} catch (Exception e) {
//				LOG.error("Not a valid productId: "+key);
//			}
//		}
//
//		LOG.info("productListIds="+productIdsList);
//
//		// Create skeleton cubeItems
//		List<CubeItem> cubeItemList = new ArrayList<CubeItem>();
//		for (String productId : productIdsList) {
//			CubeItem cubeItem = new CubeItem(productId);
//			if (cubeItem.id.equals(productId1)) {
//				cubeItem.quantity = qty1;
//			}
//			else if (params.containsKey(cubeItem.id)) {
//				cubeItem.quantity =  Integer.parseInt(params.get(cubeItem.id));
//			}
//			cubeItemList.add(cubeItem);
//		}

//		populateCubeItemDimensions(cubeItemList);
//		LOG.info("cubeItems="+cubeItemList);
//		LOG.info("cubingService.calculateCubeSpace() START");
//		CubeSpace cubeSpace = cubingService.calculateCubeSpace(cubeItemList, maxLength, maxWidth, maxHeight, maxWeight, Orientation.HORIZONTAL);
//		LOG.info("cubingService.calculateCubeSpace() DONE");
//		success = true;
//		cubingResult.setMessage(cubeSpace.toString());
//	} catch (Exception e) {
//		LOG.error(e);
//		cubingResult.setMessage(e.getMessage());
//	}
//
//	//process result
//	cubingResult.setSuccess(success);

		try {
			if (StringUtils.isBlank(maxLength)) {
				throw new IllegalArgumentException("Cannot start container if containerName is blank");
			}
			CubeSpace cubeSpace = cubingService.calculateCubeSpace(cubeList, maxLength, maxWidth, maxHeight, maxWeight);
			List<CubeSpace> resultList = Arrays.asList(cubeSpace);
			result.setResult(resultList);
			result.setMsg("result found: " + cubeSpace);
		} catch (Exception e) {
			LOG.error(e, e);
			result.setMsg(e.getMessage());
			return ResponseEntity.badRequest().body(result);
		}
		return ResponseEntity.ok(result);
	}
	
	/**
	 * JSON input defines the cube constraints, the list of {@link CubeItem} objects, and whether to populateProduct dimensions.
	 * The processing is done by {@link CubingService#calculateCubeSpace}.
	 * if populateProducts is true it assumes ids and quantities are provided, and dimensions will be populated using {@link #populateCubeItemDimensions(List)}
	 *
	 * The format of the input is:
	 * <code>
	 * {constraints: {weight:0.281,length:17.8,width:11.1,height:3.2}, cubeItems: [{id:25845880,weight:0.500,length:24.4,width:16.8,height:2.0,quantity:1},{id:29854048,weight:0.028,length:22.9,width:15.2,height:2.5,quantity:3}]}
	 * or
	 * {constraints: {weight:0.281,length:17.8,width:11.1,height:3.2}, cubeItems: [{id:616,quantity:1},{id:931,quantity:3}], populateProducts: true}
	 * </code>
	 *
	 * @return The {@link CubingResult} as a json response with success, and a message. Format is {"success":true,"message":"CubeSpace[0.0,0.0,0.0] spaces=132 cubes=2 l=284.90/300.0, w=299.20/300.0 h=16.20/30.0 volume=45.20%"}
	 * @throws ApplicationException
	 */
	@POST
	@Path("/")
	@Consumes("application/json")
	@Produces("application/json")
	public Response postCubeSpace(String input) throws ApplicationException {

		LOG.info(MessageFormat.format("post cubing with input={0}",input));

		//response DTO
		CubingResult cubingResult = new CubingResult();
		boolean success = false;

		try {
			CubingService cubingService = ServiceFactory.getInstance().getCubingService();

			try {
				CubingInputBean inputBean = new Gson().fromJson(input, CubingInputBean.class);

				CubeItem constraintCube = inputBean.getConstraints();
				if (constraintCube.length<=0 || constraintCube.width<=0 || constraintCube.height<=0) {
					throw new Exception("Constraint dimensions have not been provided correctly: "+constraintCube);
				}
				double maxLength = constraintCube.length;
				double maxWidth = constraintCube.width;
				double maxHeight = constraintCube.height;
				double maxWeight = constraintCube.weight;
				List<CubeItem> cubeItems = inputBean.getCubeItems();
				LOG.info("cubeItems="+cubeItems);
				if (inputBean.isPopulateProducts()) {
					populateCubeItemDimensions(cubeItems);
				}

				LOG.info("cubingService.calculateCubeSpace() START");
				CubeSpace cubeSpace = cubingService.calculateCubeSpace(cubeItems, maxLength, maxWidth, maxHeight, maxWeight, Orientation.HORIZONTAL);
				LOG.info("cubingService.calculateCubeSpace() DONE");
				success = true;
				cubingResult.setMessage(cubeSpace.toString());
			} catch (Exception e) {
				LOG.error(e);
				cubingResult.setMessage(e.getMessage());
			}

			//process result
			cubingResult.setSuccess(success);

		} catch (Exception e) {
			handleFatalError(e);
		}
		return buildOkResponse(cubingResult);
	}

	/**
	 * The dimensions of the products are looked up using {@link ProductDimensionsService#getProductDimensions(List)}.
	 *
	 * @param cubeItemList
	 */
	void populateCubeItemDimensions(List<CubeItem> cubeItemList) throws Exception {
		Map<Long,CubeItem> productIdMap = new HashMap<Long,CubeItem>();
		for (CubeItem cubeItem : cubeItemList) {
			productIdMap.put(Long.parseLong(cubeItem.id),cubeItem);
		}
		LOG.info("productListIds="+productIdMap.keySet());

		ProductDimensionsService productDimensionService = ServiceFactory.getInstance().getProductDimensionsService();
		List<ProductDimensionsBean> productDimensionsBeanList = productDimensionService.getProductDimensions(productIdMap.keySet());
		LOG.info("productDimensionsBeanList="+productDimensionsBeanList);
		if (productIdMap.keySet().size()>productDimensionsBeanList.size()) {
			LOG.warn("Some productIds are invalid in the database, only using "+productDimensionsBeanList.size()+"/"+productIdMap.keySet().size());
		}

		for (ProductDimensionsBean productDimensionsBean : productDimensionsBeanList) {
			CubeItem cubeItem = productIdMap.get(productDimensionsBean.getProductId());
			if (cubeItem!=null) {
				cubeItem.length = productDimensionsBean.getLength();
				cubeItem.width = productDimensionsBean.getWidth();
				cubeItem.height = productDimensionsBean.getHeight();
				cubeItem.weight = productDimensionsBean.getWeight();
			}
		}
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

	public void setCubingService(CubingService cubingService) {
		this.cubingService = cubingService;
	}
}
