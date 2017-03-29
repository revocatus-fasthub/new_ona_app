package tz.co.fasthub.ona.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.twitter.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tz.co.fasthub.ona.domain.Image;
import tz.co.fasthub.ona.domain.Payload;
import tz.co.fasthub.ona.domain.Video;
import tz.co.fasthub.ona.service.ImageService;
import tz.co.fasthub.ona.service.TalentService;
import tz.co.fasthub.ona.service.TwitterService;
import tz.co.fasthub.ona.service.VideoService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/twitter")
public class TwitterController {

    private Twitter twitter;

    private ConnectionRepository connectionRepository;

    @Autowired
    TwitterService twitterService;

    @Autowired
    TalentService talentService;

    @Autowired
    ImageService imageService;

    @Autowired
    VideoService videoService;


    final static String DOMAIN = "https://upload.twitter.com";
    final static String RESOURCE = "/1.1/media/upload.json";

    //Save the uploaded file to this folder
    private static String IMAGE_UPLOAD_ROOT = "imageUpload-dir";

    private static final String BASE_PATH = "/images";
    //private static final String BASE_PATH = "/videos";
    private static final String FILENAME = "{filename:.+}";

    private static final Logger log = LoggerFactory.getLogger(TwitterController.class);

    //String URL ="https://api.twitter.com";

    @Inject
    public TwitterController(Twitter twitter, ConnectionRepository connectionRepository) {
        this.twitter = twitter;
        this.connectionRepository = connectionRepository;
    }

    @RequestMapping(value = "/messages")
    public String index(Model model, Pageable pageable) throws IOException {
        final Page<Payload> page = twitterService.findPayloadPage(pageable);
        model.addAttribute("page", page);
        if (page.hasPrevious()) {
            model.addAttribute("prev", pageable.previousOrFirst());
        }
        if (page.hasNext()) {
            model.addAttribute("next", pageable.next());
        }
        return "/twitter/listMessages";
    }

    @RequestMapping(value = "/images")
    public String listImages(Model model, Pageable pageable){
        final  Page<Image> imagePage = imageService.findImagePage(pageable);
        model.addAttribute("imagePage", imagePage);
        if (imagePage.hasPrevious()) {
            model.addAttribute("prev", pageable.previousOrFirst());
        }
        if (imagePage.hasNext()) {
            model.addAttribute("next", pageable.next());
        }
        return "/twitter/listImage";
    }


    @RequestMapping(value = "/videos")
    public String listVideos(Model model, Pageable pageable){
        final  Page<Video> videoPage = videoService.findVideoPage(pageable);
        model.addAttribute("videoPage", videoPage);
        if (videoPage.hasPrevious()) {
            model.addAttribute("prev", pageable.previousOrFirst());
        }
        if (videoPage.hasNext()) {
            model.addAttribute("next", pageable.next());
        }
        return "/twitter/listVideos";
    }

    @RequestMapping(method=RequestMethod.GET)
    public String twitterConnection(){
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/connect/twitter";
        }
        return "/connect/twitterConnected";
    }


    @RequestMapping(value = "/viewTweets",method = RequestMethod.GET)
    public String viewTweets(Model model){
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/viewTweets";
        }
   //     log.debug("token is: "+accessGrant.getAccessToken());
        model.addAttribute(twitter.userOperations().getUserProfile());
        List<Tweet> tweets = twitter.timelineOperations().getUserTimeline();
        model.addAttribute("tweets",tweets);

        return "/twitter/viewTweets";

    }

    @RequestMapping(value = "/friends", method = RequestMethod.GET)
    public String friendList(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/viewFriendList";
        }
        model.addAttribute(twitter.userOperations().getUserProfile());
        CursoredList<TwitterProfile> friends = twitter.friendOperations().getFriends();
        model.addAttribute("friends", friends);
        return "/twitter/viewFriendList";
    }

    @RequestMapping(value = "/followers", method = RequestMethod.GET)
    public String followers(Model model) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            return "redirect:/followersList";
        }
        model.addAttribute(twitter.userOperations().getUserProfile());
        CursoredList<TwitterProfile> followers = twitter.friendOperations().getFollowers();
        model.addAttribute("followers", followers);
        return "/twitter/followersList";
    }

   //POSTING TWEET AND AN IMAGE FILE TO USER ACCOUNT
    @RequestMapping(value = "/postTweetImage",method = RequestMethod.POST)
    public String uploadAndTweetImage(@RequestParam("file") MultipartFile file, Payload payload, RedirectAttributes redirectAttributes) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            log.error("no connection to twitter");
            return "redirect:/twitter/renderPostTweet/form";
        }else if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("flash.message", "Please select a file!");
            return "redirect:/twitter/renderPostTweet/form";
        }else{
            try {
                Image image = imageService.createImage(file);

                //saving the tweet to DB
                Payload createdPayload =twitterService.savePayload(payload);

                createdPayload.setImage(image);

                twitterService.savePayload(createdPayload);

                TweetData tweetData = new TweetData(createdPayload.getMessage());
                tweetData.withMedia(imageService.findOneImage(image.getName()));

                Tweet tweet = twitter.timelineOperations().updateStatus(tweetData);

                log.info("tweet image sent");

                redirectAttributes.addFlashAttribute("flash.message", "Image Successfully uploaded");

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("flash.message", "Failed to upload image" + file.getOriginalFilename() + ": " + e);
            }
        }
        return "redirect:/twitter/images";
        }

    //POSTING TWEET AND AN VIDEO FILE TO USER ACCOUNT
    @RequestMapping(value = "/postTweetVideo",method = RequestMethod.POST)
    public String uploadAndTweetVideo(@RequestParam("videofile") MultipartFile videofile, Payload payload, RedirectAttributes redirectAttributes) {
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
            log.error("no connection to twitter");
            return "redirect:/twitter/postvideo/form";
        }else if (videofile.isEmpty()) {
            redirectAttributes.addFlashAttribute("flash.message", "Please select a video file to post!");
            return "redirect:/twitter/postvideo/form";
        }else{
            try {
                Video video = videoService.createVideo(videofile);
                //saving the tweet to DB
                Payload createdPayload =twitterService.savePayload(payload);

                createdPayload.setVideo(video);
                twitterService.savePayload(createdPayload);

                TweetData tweetData = new TweetData(createdPayload.getMessage());
                tweetData.withMedia(videoService.findOneVideo(video.getName()));


                Tweet tweet = twitter.timelineOperations().updateStatus(tweetData);
                log.info("tweet sent");

                redirectAttributes.addFlashAttribute("flash.message", "Video Successfully uploaded");

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("flash.message", "Failed to upload Video " + videofile.getOriginalFilename() + ": " + e);
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("flash.message", "Uncaught Exception: " + e);
            }
        }
        return "redirect:/twitter/videos";
    }

    @RequestMapping(method=RequestMethod.DELETE, value = BASE_PATH + "/images/" + FILENAME)
    public String deleteImage(@PathVariable String filename, RedirectAttributes redirectAttributes) throws IOException {
        try {
            imageService.deleteImage(filename);
            redirectAttributes.addFlashAttribute("flash.message", "Image Successfully deleted " + filename + " from the server");
        } catch (IOException|RuntimeException e) {
            redirectAttributes.addFlashAttribute("flash.message", "Failed to delete Image " + filename + " => " + e.getMessage());
        }
        return "redirect:/twitter/images";
    }

/*

    @RequestMapping(method=RequestMethod.DELETE, value = BASE_PATH + "/" + FILENAME)
    public String deleteVideo(@PathVariable String filename, RedirectAttributes redirectAttributes) throws IOException {
        try {
            videoService.deleteVideo(filename);
            redirectAttributes.addFlashAttribute("flash.message", "Video Successfully deleted " + filename + "from the server");
        } catch (IOException|RuntimeException e) {
            redirectAttributes.addFlashAttribute("flash.message", "Failed to delete Video" + filename + " => " + e.getMessage());
        }
        return "redirect:/twitter/videos";
    }

 */

}