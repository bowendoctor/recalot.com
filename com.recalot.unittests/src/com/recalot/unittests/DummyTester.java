package com.recalot.unittests;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author Matthäus Schmedding (info@recalot.com)
 */
public class DummyTester {

    @Test
    public void dateTimeTest() {
        String xmlDateTimeString = "2015-08-10T13:37:24-07:00";
        Date date = DatatypeConverter.parseDateTime(xmlDateTimeString).getTime();

        System.out.println(date.getTime());


        Date modifiedDate = new Date(date.getTime() + 10);
        System.out.println(modifiedDate.getTime());
    }

    @Test
    public void parseIntTest() {
        ArrayList<String> test = new ArrayList<>();
        for(int i = 0; i < 100000; i++) {
            test.add("asd" + i);
        }

        StopWatch sw = new StopWatch();

        sw.start();

        for(String t : test) {
            try {
                int r = Integer.parseInt(t, 10);

            } catch (NumberFormatException e) {

            }
        }

        sw.stop();

        System.out.println(sw.getTime());
    }


    @Test
    public void testWordSplit() {
        String sentence = "Here&#39;s an idea. Open the articles and READ before you spread this nonsense. For instance, your very first link, the ICM Poll says this: They don&#39;t sympathize with the bombers, they sympathize with the &quot;feelings and motives&quot; which is an entirely different thing and could mean anything, from &quot;feelings&quot; of estrangement in society and &quot;motives&quot; of highlighting Western foreign policy in Iraq. 99% consider the terrorist attack WRONG.Your second link asserts that 25% of British Muslims think the bombing was justified, yet your first link says 99% are against it. So which one do we believe? Both are news stories and neither provide links to the actual study.Your third link is about the Iraq war and yes, plenty of people think that American aggression in that country was unjustified and the only legitimate way for them to defend themselves was suicide bombings. I don&#39;t support this, but we can hardly scrutinize people for not adhering to non-violence. Indeed, about 60% of Americans supported the initial attack on Iraq war and bought into the WMD lies which resulted in massive war crimes and the deaths of hundreds of thoudands of civilians and the creation of a vaccuum where millions of people were killed in sectarian violence and groups like ISIS were able to flourish.  Link 4 doesn&#39;t exist.Link 5 is the same as link 3, specially referring to American troops in Iraq. It&#39;s unfortunate that people think that violence is justfied but war is brutal and people generally support one side or the other. Again, nearly 60% of American civilians &quot;supported attack on Iraqi people&quot; before the Iraq invasion. If you refer to page 5 of the document though, you will find that the overwhelmingly large majorities in the Muslim world are against attacking U.S. civilians in general.link 5 and 6 is about Hezbollah and Hamas, again politics is involved and I support neither of those groups and consider them terrorists, but you have to realize that the Palestinian and Israeli issue is a polarizing one. If you look at some of the polls such as this where 44% of Americans think that Israeli actions against Palestinians are justified which would mean that they justify illegal settlements and bombings. Quite tragic.links 7 to 18 do not represent Muslim majorities and in fact the majority believes that violence is not justified according to those very polls. A bunch of them are not in english so I was not able to analyse it. 14, 15 and 16 don&#39;t exist.19 is problematic, and part of the reason why Palestine/Israel issue is not a simple one and it breaks my heart that people will give in to their hatred.The last one, 13% is still a minority who support these terrorist groups. Quite unfortunate, but a minority is a minority. Moreover, the complete finding of this particular poll is that Muslims garner little support for terrorists and are themselves concerned about religious extremism.I would urge you to consider these points before you copy and paste it to another thread. Most people do not care to read the documents and articles and would rather just read the title and make their judgement about Muslims. They will not consider the context and the political dynamics that generates such opinions, something which is essential to understanding a poll.";

        sentence = StringEscapeUtils.unescapeHtml4(sentence);
        String[] parts = sentence.split("(?<!^)\\b");

        String last = null;
        boolean concat = false;

        for (String word : parts) {
            if (last != null) {
                if (word.trim().length() == 0) {
                    System.out.println(last);
                    last = null;
                } else if ( (!concat && !word.equals("'") && last != null)) {
                    System.out.println(last);
                    last = word;
                } else if (word.equals("'")) {
                    concat = true;
                    last += word;
                } else {
                    if(concat) {
                        last += word;
                    } else {
                        last = word;
                    }
                    concat = false;
                }
            } else {
                last = word;
            }
        }

        if(last != null) {
            System.out.println(last);
        }

        System.out.println(parts.length);
    }


    @Test
    public void redditDataSetAnalyser() {

        String path = "C:\\Privat\\3_Uni\\5_Workspaces\\data\\reddit-light-500";


        int users = 0;
        int wordCount = 0;
        int comments = 0;
        int interactions = 0;

        int minInteractionsPerUser = Integer.MAX_VALUE;
        int maxInteractionsPerUser = 0;

        int minCommentsPerUser = Integer.MAX_VALUE;
        int maxCommentsPerUser = 0;

        File dir = new File(path);


        HashMap<String, Boolean> words = new HashMap<>();
        for (File userFile : dir.listFiles()) {
            if (userFile.getName().endsWith(".csv")) {

                int commentsOfUser = 0;
                int interactionsOfUser = 0;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(userFile)))) {
                    String line = null;

                    while ((line = reader.readLine()) != null) {

                        String[] split = line.split("00;");

                        if (split.length >= 2) {
                            List<String> wordList = splitIntoWords(split[1]);
                            if(wordList.size() > 0) {
                                commentsOfUser++;

                                interactionsOfUser += wordList.size();

                                for (String word : wordList) {
                                    if (!words.containsKey(word)) {
                                        words.put(word, true);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException x) {
                    x.printStackTrace();
                }

                if(commentsOfUser == 2) System.out.println(userFile.getName());
                if(minCommentsPerUser > commentsOfUser) minCommentsPerUser = commentsOfUser;
                if(maxCommentsPerUser < commentsOfUser) maxCommentsPerUser = commentsOfUser;

                if(minInteractionsPerUser > interactionsOfUser) minInteractionsPerUser = interactionsOfUser;
                if(maxInteractionsPerUser < interactionsOfUser) maxInteractionsPerUser = interactionsOfUser;

                interactions += interactionsOfUser;
                comments += commentsOfUser;
                users++;
            }
        }

        System.out.println("Users:" + users);
        System.out.println("Words:" + words.size());
        System.out.println("Words Usages:" + interactions);
        System.out.println("Comments:" + comments);
        System.out.println("Min Comments Per User:" + minCommentsPerUser);
        System.out.println("Max Comments Per User:" + maxCommentsPerUser);
        System.out.println("Avg. Comments Per User:" + (1.0 * comments / users));
        System.out.println("Min Word Usages Per User:" + minInteractionsPerUser);
        System.out.println("Max Word Usages Per User:" + maxInteractionsPerUser);
        System.out.println("Avg. Word Usages Per User:" + (1.0 * interactions / users));
    }

    private List<String> splitIntoWords(String sentence) {
        List<String> words = new ArrayList<>();
        sentence = StringEscapeUtils.unescapeHtml4(sentence);
        String[] parts = sentence.split("(?<!^)\\b");

        String last = null;
        boolean concat = false;

        for (String word : parts) {
            if (last != null) {
                if (word.trim().length() == 0) { //spaces? add the last word
                    words.add(last.trim().toLowerCase());
                    last = null;
                } else if ((!concat && !word.equals("'") && last != null)) {  //punctuation? add the last word and set punctuation as last word
                    words.add(last.trim().toLowerCase());
                    last = word;
                } else if (word.equals("'")) { //quotes? concat the following words
                    concat = true;
                    last += word;
                } else {
                    if (concat) {
                        last += word;
                    } else {
                        last = word;
                    }
                    concat = false;
                }
            } else {
                last = word;
            }
        }

        if (last != null) { //a word in pipeline
            words.add(last.trim().toLowerCase());
        }

        return words;
    }
}
