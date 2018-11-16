package anoop.h.webflux;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * @author Anoop Hallimala 2018
 */
@Service
public class RestaurantService {
	
	private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @NewSpan("byPriceReactive")
    public Flux<Restaurant> byPrice(Double maxPrice) {
    	logger.debug("inside byPrice Reactive");
        return Flux.fromArray(new Restaurant[] { new Restaurant("McDonalds", 1) });
    }
    
    @NewSpan("byPriceMVC")
    public List<Restaurant> byPriceMVC(Double maxPrice) {
    	logger.debug("inside byPrice MVC");
        return Arrays.asList(new Restaurant[] { new Restaurant("McDonalds", 1) });
    }
}
