package gov.tn.dhs.ecm.service;

import gov.tn.dhs.ecm.model.Blog;
import gov.tn.dhs.ecm.model.Post;
import gov.tn.dhs.ecm.model.PostCreationRequest;
import gov.tn.dhs.ecm.model.PostCreationResponse;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateBlogPostService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(CreateBlogPostService.class);

    public void process(Exchange exchange) {
        PostCreationRequest postCreationRequest = exchange.getIn().getBody(PostCreationRequest.class);
        String userId = postCreationRequest.getUserId();
        String title = postCreationRequest.getTitle();
        String body = postCreationRequest.getBody();

        if ((userId == null) && (title == null) && (body == null)) {
            setupError("400", "No information provided");
        }

        try {
            int size = Blog.getSize();
            String id = Integer.toString(size+1);
            Post post = new Post(id, userId, title, body);
            Blog.addPost(id, post);
            PostCreationResponse postCreationResponse = new PostCreationResponse();
            postCreationResponse.setId(id);
            setupResponse(exchange, "200", postCreationResponse);
        } catch (Exception e) {
            setupError("500", "Service error");
        }
    }

}



