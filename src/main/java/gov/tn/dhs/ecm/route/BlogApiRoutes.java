package gov.tn.dhs.ecm.route;

import com.fasterxml.jackson.core.JsonParseException;
import gov.tn.dhs.ecm.exception.ServiceErrorException;
import gov.tn.dhs.ecm.model.PostCreationRequest;
import gov.tn.dhs.ecm.model.PostCreationResponse;
import gov.tn.dhs.ecm.model.SimpleMessage;
import gov.tn.dhs.ecm.service.CreateBlogPostService;
import gov.tn.dhs.ecm.service.GetBlogPostsService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
class BlogApiRoutes extends RouteBuilder {

    @Value("${server.port}")
    private String serverPort;

    @Value("${runstatus}")
    private String runStatus;

    private final GetBlogPostsService getBlogPostsService;
    private final CreateBlogPostService createBlogPostService;

    public BlogApiRoutes(GetBlogPostsService getBlogPostsService, CreateBlogPostService createBlogPostService) {
        this.getBlogPostsService = getBlogPostsService;
        this.createBlogPostService = createBlogPostService;
    }

    @Override
    public void configure() {

        onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{}"))
        ;

        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("${exception.message}"))
        ;

        onException(ServiceErrorException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("${exception.code}"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("${exception.message}"))
        ;

        restConfiguration()
                .enableCORS(true)
                .apiProperty("cors", "true") // cross-site
                .component("servlet")
                .port(serverPort)
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        defineStatusPath();

        defineGetBlogPostsPath();

        defineCreateBlogPost();

    }

    private void defineStatusPath() {
        SimpleMessage simpleMessage = new SimpleMessage(runStatus);
        rest()
                .get("/")
                .to("direct:runningStatus")
        ;
        from("direct:runningStatus")
                .log("Status request sent")
                .log("runstatus property value is " + runStatus)
                .process(exchange -> exchange.getIn().setBody(simpleMessage, SimpleMessage.class))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader("Content-Type", constant("application/json"))
                .endRest()
        ;
    }

    private void defineGetBlogPostsPath() {
        rest()
                .get("/posts")
                .outType(List.class)
                .to("direct:getBlogPostsService")
        ;
        from("direct:getBlogPostsService")
                .routeId("getBlogPostsService")
                .bean(getBlogPostsService)
                .endRest()
        ;
    }

    private void defineCreateBlogPost() {
        rest()
                .post("/posts")
                .type(PostCreationRequest.class)
                .outType(PostCreationResponse.class)
                .to("direct:createBlogPostService")
        ;
        from("direct:createBlogPostService")
                .routeId("createBlogPostService")
                .bean(createBlogPostService)
                .endRest()
                ;
    }

}
