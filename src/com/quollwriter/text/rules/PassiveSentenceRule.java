package com.quollwriter.text.rules;

import java.util.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.components.*;

import org.jdom.*;


public class PassiveSentenceRule extends AbstractSentenceRule
{

    public static final String CREATE_TYPE = "passivesentence";

    public class XMLConstants
    {

        public static final String beWords = "beWords";
        public static final String irregularForms = "irregularForms";

    }

    private Map<String, String> beWords = new HashMap ();
    private Map<String, String> irregularForms = new HashMap ();

    public PassiveSentenceRule(boolean user)
    {

        super (user);

    }

    public String getDescription ()
    {

        String d = super.getDescription ();

        return d;

    }

    public String getSummary ()
    {

        return super.getSummary ();

    }

    public String getCreateType ()
    {

        return PassiveSentenceRule.CREATE_TYPE;

    }

    public void init (Element root)
               throws JDOMException
    {

        super.init (root);

        String sw = JDOMUtils.getAttributeValue (root,
                                                 XMLConstants.beWords);

        StringTokenizer t = new StringTokenizer (sw,
                                                 ",");

        while (t.hasMoreTokens ())
        {

            this.beWords.put (t.nextToken ().trim ().toLowerCase (),
                              "");


        }

        String w = JDOMUtils.getAttributeValue (root,
                                                XMLConstants.irregularForms);

        t = new StringTokenizer (w,
                                 ",");

        while (t.hasMoreTokens ())
        {

            this.irregularForms.put (t.nextToken ().trim ().toLowerCase (),
                                     "");


        }

    }

    public Element getAsElement ()
    {

        Element root = super.getAsElement ();

        return root;

    }

    public List<Issue> getIssues (String  sentence,
                                  boolean inDialogue)
    {

        List<Issue> issues = new ArrayList ();

        // Check our list of words.
        sentence = sentence.toLowerCase ();

        List<String> swords = TextUtilities.getAsWords (sentence);

        int c = 0;
        int lastWord = -1;

        for (int i = 0; i < swords.size (); i++)
        {

            String a = swords.get (i);

            if (this.beWords.containsKey (a))
            {

                // Check the next word.
                if (i < swords.size ())
                {

                    String b = swords.get (i + 1);

                    if (this.isPastTenseVerb (b))
                    {

                        Issue iss = new Issue ("Passive voice, contains: <b>" + a + " " + b + "</b>",
                                               i,
                                               a.length () + b.length () + 1,
                                               this);

                        issues.add (iss);


                    }

                }

            }

        }

        return issues;

    }

    public boolean isPastTenseVerb (String w)
    {

        // See if the word is:
        // 1. A verb
        // 2. Ends with "ed" OR:
        // 3. Is an irregular form.
        if (this.irregularForms.containsKey (w))
        {

            return true;

        }

        // At this point we only consider words that end with "ed".
        if (!w.endsWith ("ed"))
        {

            return false;

        }

        w = w.substring (0,
                         w.length () - 2);

        // Check to see if the word is valid, if not add the e back. (i.e. damaged -> damage)
        try
        {

            if (Environment.getWordTypes (w) == null)
            {

                w += "e";

            }

        } catch (Exception e)
        {


        }

        String t = null;

        try
        {

            t = Environment.getWordTypes (w);

        } catch (Exception e)
        {

            Environment.logError ("Unable to get word types for word: " +
                                  w,
                                  e);

            return false;

        }

        if (t == null)
        {

            return false;

        }

        if ((t.indexOf (Synonyms.VERB) != -1) ||
            (t.indexOf (Synonyms.VERB_T) != -1) ||
            (t.indexOf (Synonyms.VERB_I) != -1))
        {

            return true;

        }

        return false;

    }

    public List<FormItem> getFormItems ()
    {

        List<FormItem> items = new ArrayList ();

        return items;

    }

    public void updateFromForm ()
    {

    }

    public String getCategory ()
    {

        return Rule.SENTENCE_CATEGORY;

    }

}