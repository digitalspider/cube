package au.com.digitalspider.cube.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.omg.CORBA.portable.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.digitalspider.cube.bean.CubeItem;
import au.com.digitalspider.cube.bean.CubeSpace;
import au.com.digitalspider.cube.bean.CubingInput;
import au.com.digitalspider.cube.bean.CubingOutput;
import au.com.digitalspider.cube.bean.Orientation;
import au.com.digitalspider.cube.service.CubingService;
import au.com.digitalspider.cube.service.SlottingService;

@RequestMapping("/cube")
@Component
public class CubingController {

	private static Logger LOG = Logger.getLogger(CubingController.class);

	@Autowired
	private CubingService cubingService;
	@Autowired
	private SlottingService slottingService;

	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	/**
	 * Determines if products fit in the dimensions given.
	 *
	 * Information is provided by url parameters in the format: /{maxLength}/{maxWidth}/{maxHeight}/{maxWeight}/{itemLength}/{itemWidth}/{itemHeight}/{itemWeight}/{itemQuantity}
	 * Example:
	 * <ul>
	 * 	<li>http://localhost:8080/cube/25/25/3.5/7.0/4.5/2.1/1.0/50.0/7</li>
	 * </ul>
	 * The above example processes:
	 * <ul>
	 * 	<li>constraints: maxLength=25, maxWidth=25, maxHeight=3.5 and weight=7.0</li>
	 *  <li>product: maxLength=4.5, maxWidth=2.1, maxHeight=1.0 and weight=50.0 x7</li>
	 * </ul>
	 * The dimensions of the product are converted into {@link CubeItems} 
	 * and processed using {@link CubingService#calculateCubeSpace}
	 *
	 * @param constraints json input with format {weight:0.281,length:17.8,width:11.1,height:3.2}
	 * @param cubes json input with format [{id:25845880,weight:0.500,length:24.4,width:16.8,height:2.0,quantity:1},{id:29854048,weight:0.028,length:22.9,width:15.2,height:2.5,quantity:3}]
	 * @return The {@link CubeSpace} as a json response with success, and a message. Format is {"success":true,"message":"CubeSpace[0.0,0.0,0.0] spaces=132 cubes=2 l=284.90/300.0, w=299.20/300.0 h=16.20/30.0 volume=45.20%"}
	 * @throws ApplicationException
	 */
	@RequestMapping(value = "/{maxLength}/{maxWidth}/{maxHeight}/{maxWeight}/{itemLength}/{itemWidth}/{itemHeight}/{itemWeight}/{itemQuantity}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<?> willCubeFit(HttpServletRequest request, 
			@PathVariable double maxLength, @PathVariable double maxWidth, @PathVariable double maxHeight, @PathVariable double maxWeight, 
			@PathVariable double itemLength, @PathVariable double itemWidth, @PathVariable double itemHeight, @PathVariable double itemWeight, 
			@PathVariable int itemQuantity) {
		AjaxResponseBody<CubingOutput> result = new AjaxResponseBody<>();

		LOG.info(MessageFormat.format("Test cubing with length={0},width={1},height={2},weight={3}, and length={4},width={5},height={6},weight={7}, qty={8}", maxLength,maxWidth,maxHeight,maxWeight, itemLength,itemWidth,itemHeight,itemWeight,itemQuantity));
		boolean success = false;

		// Create skeleton cubeItems
		List<CubeItem> cubeItemList = new ArrayList<CubeItem>();
		CubeItem cubeItem = new CubeItem("single");
		cubeItem.length = itemLength;
		cubeItem.width = itemWidth;
		cubeItem.height = itemHeight;
		cubeItem.quantity = itemQuantity;
		cubeItemList.add(cubeItem);

		try {
			LOG.info("cubeItems="+cubeItemList);
			LOG.info("cubingService.calculateCubeSpace() START");
			CubeSpace cubeSpace = cubingService.calculateCubeSpace(cubeItemList, maxLength, maxWidth, maxHeight, maxWeight, Orientation.HORIZONTAL);
			LOG.info("cubingService.calculateCubeSpace() DONE");
			success = true;
			result.setResult(new CubingOutput().setCubeSpace(cubeSpace));
			result.setMsg("success: " + cubeSpace.toString());
		} catch (Exception e) {
			LOG.error(e, e);
			result.setMsg("failure: "+e.getMessage());
			return ResponseEntity.badRequest().body(result);
		}

		return ResponseEntity.ok(result);
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
	@RequestMapping(value = "/{maxLength}/{maxWidth}/{maxHeight}/{maxWeight}/{itemLength}/{itemWidth}/{itemHeight}/{itemWeight}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<?> howManyCubes(HttpServletRequest request, 
			@PathVariable double maxLength, @PathVariable double maxWidth, @PathVariable double maxHeight, @PathVariable double maxWeight, 
			@PathVariable double itemLength, @PathVariable double itemWidth, @PathVariable double itemHeight, @PathVariable double itemWeight) {
		AjaxResponseBody<CubingOutput> result = new AjaxResponseBody<>();

		LOG.info(MessageFormat.format("Test cubing with length={0},width={1},height={2},weight={3}, and length={4},width={5},height={6},weight={7}", maxLength,maxWidth,maxHeight,maxWeight, itemLength,itemWidth,itemHeight,itemWeight));

		// Create skeleton cubeItems
		List<CubeItem> cubeItemList = new ArrayList<CubeItem>();
		CubeItem cubeItem = new CubeItem("single");
		cubeItem.length = itemLength;
		cubeItem.width = itemWidth;
		cubeItem.height = itemHeight;
		cubeItem.quantity = 1;
		int calcQuantity = (int)(maxWidth/itemWidth)*(int)(maxLength/itemLength)*(int)(maxHeight/maxHeight);
		int calcQuantityForWeight = (int)(maxWeight/itemWeight);
		calcQuantity = Math.min(calcQuantity, calcQuantityForWeight);
		if (calcQuantity>1) {
			cubeItem.quantity=calcQuantity;
		}
		cubeItemList.add(cubeItem);

		try {
			LOG.info("cubeItems="+cubeItemList);
			LOG.info("cubingService.calculateCubeSpace() START");
			CubeSpace cubeSpace = null;
			while (true) {
				try {
					cubeSpace = cubingService.calculateCubeSpace(cubeItemList, maxLength, maxWidth, maxHeight, maxWeight, Orientation.HORIZONTAL);
					cubeItem.quantity+=1;
				} catch (Exception e) {
					break;
				}
			}
			int resultQuantity = cubeItem.quantity;
			LOG.info("cubingService.calculateCubeSpace() DONE. resultQuantity="+resultQuantity);
			result.setResult(new CubingOutput().setCubeSpace(cubeSpace));
			result.setMsg("success. qty="+resultQuantity+" space=" + cubeSpace);
		} catch (Exception e) {
			LOG.error(e, e);
			result.setMsg("failure: "+e.getMessage());
			return ResponseEntity.badRequest().body(result);
		}
		return ResponseEntity.ok(result);
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
	@RequestMapping(value = "/{maxLength}/{maxWidth}/{maxHeight}/{maxWeight}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<?> willCubesFit(HttpServletRequest request, @PathVariable double maxLength,
			@PathVariable double maxWidth, @PathVariable double maxHeight, @PathVariable double maxWeight, 
			@Valid @RequestBody CubingInput cubingInput, Errors errors) {
		AjaxResponseBody<CubingOutput> result = new AjaxResponseBody<>();

		LOG.info(MessageFormat.format("Cubing with length={0},width={1},height={2},weight={3}, and items={4}", maxLength,maxWidth,maxHeight,maxWeight,cubingInput.getCubeItems()));
		boolean success = false;

		try {
			List<CubeItem> cubeItemList = cubingInput.getCubeItems();
			LOG.info("cubeItems="+cubeItemList);
			LOG.info("cubingService.calculateCubeSpace() START");
			CubeSpace cubeSpace = cubingService.calculateCubeSpace(cubeItemList, maxLength, maxWidth, maxHeight, maxWeight, Orientation.HORIZONTAL);
			LOG.info("cubingService.calculateCubeSpace() DONE");
			success = true;
			result.setResult(new CubingOutput().setCubeSpace(cubeSpace));
			result.setMsg("result found: " + cubeSpace);
		} catch (Exception e) {
			LOG.error(e, e);
			result.setMsg(e.getMessage());
			return ResponseEntity.badRequest().body(result);
		}
		return ResponseEntity.ok(result);
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
	@RequestMapping(value = "/slot", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<?> slot(HttpServletRequest request, @Valid @RequestBody CubingInput cubingInput, Errors errors) {
		AjaxResponseBody<CubingOutput> result = new AjaxResponseBody<>();

		LOG.info(MessageFormat.format("Slotting with spaces={0},items={1}", cubingInput.getCubeSpaces(),cubingInput.getCubeItems()));
		try {
			List<CubeItem> cubeItemList = cubingInput.getCubeItems();
			LOG.info("cubeItems="+cubeItemList);
			LOG.info("cubingService.calculateCubeSpace() START");
			CubeSpace cubeSpace = slottingService.findCubeSpace(cubingInput.getCubeSpaces(), cubingInput.getCubeItems());
			LOG.info("cubingService.calculateCubeSpace() DONE");
			result.setResult(new CubingOutput().setCubeSpace(cubeSpace));
			result.setMsg("result found: " + cubeSpace);
		} catch (Exception e) {
			LOG.error(e, e);
			result.setMsg(e.getMessage());
			return ResponseEntity.badRequest().body(result);
		}
		return ResponseEntity.ok(result);
	}	
	

	public void setCubingService(CubingService cubingService) {
		this.cubingService = cubingService;
	}

	public void setSlottingService(SlottingService slottingService) {
		this.slottingService = slottingService;
	}
}
