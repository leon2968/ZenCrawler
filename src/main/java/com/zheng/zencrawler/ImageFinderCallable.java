package com.zheng.zencrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 The ImageFinderCallable Class is created to search images for a given URL and its subpages
 @author Leon Zheng
 */
public class ImageFinderCallable implements Callable<List<String>> {

    private final String url;
    private ConcurrentHashSet<String> visitedLinks, linksToVisit;


    public ImageFinderCallable(String url, ConcurrentHashSet<String> visitedLinks, ConcurrentHashSet<String> linksToVisit) {
        this.url = url;
        this.visitedLinks = visitedLinks;
        this.linksToVisit = linksToVisit;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> images = new ArrayList<>();
        Document doc = Jsoup.connect(url)
                            .ignoreContentType(true)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .referrer("http://www.google.com")
                            .timeout(12000)
                            .followRedirects(true)
                            .get();

//        if (isVisited(url)){
//            System.out.println("Visited Url");
//        }
//
//        if (!isAllowedByRobotsTxt(url)) {
//            System.out.println("RobotsTxt not allowed");
//        }

        //fetch images from url
        if (!isVisited(url)&&isAllowedByRobotsTxt(url)) {
            //System.out.println("Unvisited url, now fetching images...");
            images.addAll(getImages(doc));
        }
        visitedLinks.add(url);

        //update linksToVisit
        for (Element link : getSubpageLinks(doc)) {
            String subpageUrl = link.attr("abs:href");
            if (isInternalLink(subpageUrl) && !isVisited(subpageUrl)) {
                //System.out.println("adding new subpage..."+subpageUrl);
                linksToVisit.add(subpageUrl);

            }
        }
        return images;
    }

    private Document connect(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    private Elements getSubpageLinks(Document doc) {
        return doc.select("a[href]");
    }

    private boolean isInternalLink(String linkUrl) {
        return linkUrl.startsWith(url);
    }

    private boolean isVisited(String linkUrl) {
        return visitedLinks.contains(linkUrl);
    }

    private boolean isAllowedByRobotsTxt(String link) {
        try {
            URL url = new URL(link);
            String host = url.getHost();
            URL robotsTxtUrl = new URL("https://" + host + "/robots.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(robotsTxtUrl.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Disallow")) {
                    String disallowedPath = line.substring("Disallow:".length()).trim();
                    if (url.getPath().startsWith(disallowedPath)) {
                        reader.close();
                        return false;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            // If an exception is thrown, assume the URL is allowed by robots.txt
            e.printStackTrace();
        }
        return true;
    }

    private List<String> getImages(Document doc) {
        List<String> imageUrls = new ArrayList<>();
        Elements images = doc.select("img");
        if(images.isEmpty()) {
            //System.out.println("no images found in this url");
        }
        for (Element image : images) {
            String imageUrl = image.absUrl("src");
            //System.out.println("getting absUrl for the images....");
            if (!imageUrl.isEmpty()) {
                //System.out.println("adding image url...." + imageUrl);
                imageUrls.add(imageUrl);
            }
        }
        return imageUrls;
    }

}