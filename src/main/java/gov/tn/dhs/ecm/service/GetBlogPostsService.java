package gov.tn.dhs.ecm.service;

import gov.tn.dhs.ecm.model.Blog;
import gov.tn.dhs.ecm.model.Post;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class GetBlogPostsService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(GetBlogPostsService.class);

    public void process(Exchange exchange) {
        try {
            List<Post> posts = new LinkedList<>();
            for (String id : Blog.getStore().keySet()) {
                Post post = Blog.getPost(id);
                posts.add(post);
            }
            logger.info("Number of entries = {}", posts.size());
            setupResponse(exchange, "200", posts);
        } catch (Exception e) {
            setupError("500", "Service error");
        }
    }

}



