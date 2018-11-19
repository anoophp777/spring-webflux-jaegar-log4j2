package anoop.h.webflux;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.ContinueSpan;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.opentracing.Tracer;
import reactor.core.publisher.Flux;

/**
 * @author Anoop Hallimala 2018
 */
@RestController
public class RestaurantController {

	private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);

	@Autowired
	private RestaurantService restaurantService;
	
    @Autowired private Tracer tracer;

	
	WebClient webClient = WebClient.create("http://localhost:7070");

    @RequestMapping("/chaining")
    @ContinueSpan
    public Flux<Restaurant> chaining() {
    	return callByPriceReactive();
    }
    
    @NewSpan("chainingPriceReactive")
    private Flux<Restaurant> callByPriceReactive(){
    	return webClient.get().uri("/byPriceReactive?maxPrice=1").retrieve().bodyToFlux(Restaurant.class).checkpoint();
    }

	@GetMapping("/byPriceReactive")
	@ContinueSpan
	public Flux<Restaurant> byPriceReactive(@RequestParam Double maxPrice) {
		
		log.debug("tracer: "+tracer);
		log.info("active span: "+tracer.activeSpan());
		// just a log statement to show the current context
		log.debug("starting statement");

		return restaurantService
				.byPrice(maxPrice)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(r -> log.debug("found restaurant {} for ${}", r.getName(), r.getPricePerPerson()))
				.doOnComplete(() -> log.debug("done!"))
				.doOnError(e -> log.error("failure", e));
	}
	
	@GetMapping("/byPriceMVC")
	@ContinueSpan
	public List<Restaurant> byPriceMVC(@RequestParam Double maxPrice) {
		
		log.debug("tracer: "+tracer);
		log.info("active span: "+tracer.activeSpan());
		
		// just a log statement to show the current context
		log.debug("starting statement");

		return restaurantService
				.byPriceMVC(maxPrice);
	}

}
