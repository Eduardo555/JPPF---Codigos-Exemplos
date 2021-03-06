/*
 * JPPF.
 * Copyright (C) 2005-2014 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jppf.ui.options.factory;

import java.awt.Frame;
import java.util.*;
import java.util.prefs.*;

import javax.swing.JFrame;

import org.jppf.ui.options.*;
import org.jppf.ui.options.xml.OptionsPageBuilder;
import org.slf4j.*;

/**
 * This class handles the persistence of the dynamic UI com
 * @author Laurent Cohen
 */
public final class OptionsHandler
{
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(OptionsHandler.class);
  /**
   * The root of the preferences subtree in which the chart configurations are saved.
   */
  private static final Preferences JPPF_PREFERENCES = Preferences.userRoot().node("jppf");
  /**
   * The root of the preferences subtree in which the chart configurations are saved.
   */
  private static Preferences preferences = JPPF_PREFERENCES;
  /**
   * The list of option pages managed by this handler.
   */
  private static List<OptionElement> pageList = new Vector<>();
  /**
   * A mapping of option pages to their name.
   */
  private static Map<String, OptionElement> pageMap = new Hashtable<>();
  /**
   * The page builder used to instantiate pages from XML descriptors.
   */
  private static OptionsPageBuilder builder = new OptionsPageBuilder();
  /**
   * The main window of the application.
   */
  private static JFrame mainWindow = null;

  /**
   * Get the list of option pages managed by this handler.
   * @return a list of <code>OptionsPage</code> instances.
   */
  public static List<OptionElement> getPageList()
  {
    return pageList;
  }

  /**
   * Retrieve a page from its name.
   * @param name the name of the page to retrieve.
   * @return an <code>OptionsPage</code> instance.
   */
  public static synchronized OptionElement getPage(final String name)
  {
    return pageMap.get(name);
  }

  /**
   * Add a page to the list of pages managed by this handler.
   * @param page an <code>OptionsPage</code> instance.
   * @return the page that was added.
   */
  public static synchronized OptionElement addPage(final OptionElement page)
  {
    pageList.add(page);
    try
    {
      pageMap.put(page.getName(), page);
    }
    catch(RuntimeException e)
    {
      int breakpoint = 0;
      log.info("Exception for page = \"" + page + "\" : " + e.getMessage());
      throw e;
    }
    return page;
  }

  /**
   * Remove a page from the list of pages managed by this handler.
   * @param page an <code>OptionsPage</code> instance.
   */
  public static synchronized void removePage(final OptionContainer page)
  {
    pageList.remove(page);
    pageMap.remove(page.getName());
  }

  /**
   * Load a page built from an xml document.
   * @param xmlPath the path to the xml document.
   * @return the page that was added.
   */
  public static synchronized OptionElement loadPageFromXml(final String xmlPath)
  {
    try
    {
      return builder.buildPage(xmlPath, null);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Add a page built from an xml document.
   * @param xmlPath the path to the xml document.
   * @return the page that was added.
   */
  public static synchronized OptionElement addPageFromXml(final String xmlPath)
  {
    return addPage(loadPageFromXml(xmlPath));
  }

  /**
   * Load a page built from an xml document.
   * @param xmlPath the path to the xml document.
   * @param baseName base name for resource bundle lookup.
   * @return the page that was added.
   */
  public static synchronized OptionElement loadPageFromURL(final String xmlPath, final String baseName)
  {
    try
    {
      return builder.buildPageFromURL(xmlPath, baseName);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Add a page built from an xml document.
   * @param xmlPath the path to the xml document.
   * @param baseName base name for resource bundle lookup.
   * @return the page that was added.
   */
  public static synchronized OptionElement addPageFromURL(final String xmlPath, final String baseName)
  {
    return addPage(loadPageFromURL(xmlPath, baseName));
  }

  /**
   * Save the value of all persistent options in the preferences store.
   */
  public static void savePreferences()
  {
    try
    {
      for (OptionElement elt: pageList)
      {
        OptionNode node = buildPersistenceGraph(elt);
        savePreferences(node, getPreferences());
      }
      getPreferences().flush();
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Save the value of all persistent options in the preferences store.
   * @param node the root of the options subtree to save.
   * @param prefs the preferences node in which to save the values.
   */
  public static void savePreferences(final OptionNode node, final Preferences prefs)
  {
    if (!node.children.isEmpty())
    {
      Preferences p = prefs.node(node.elt.getName());
      for (OptionNode child: node.children) savePreferences(child, p);
    }
    else if (node.elt instanceof Option)
    {
      Option option = (Option) node.elt;
      if (option.isPersistent()) prefs.put(option.getName(), String.valueOf(option.getValue()));
    }
  }

  /**
   * Load the value of all persistent options in the preferences store.
   */
  public static void loadPreferences()
  {
    for (OptionElement elt: pageList)
    {
      OptionNode node = buildPersistenceGraph(elt);
      loadPreferences(node, getPreferences());
    }
  }

  /**
   * Save the value of all persistent options in the preferences store.
   * @param node the root of the options subtree to save.
   * @param prefs the preferences node in which to save the values.
   */
  public static void loadPreferences(final OptionNode node, final Preferences prefs)
  {
    //if (node == null) return;
    if (!node.children.isEmpty())
    {
      Preferences p = prefs.node(node.elt.getName());
      for (OptionNode child: node.children) loadPreferences(child, p);
    }
    else if (node.elt instanceof AbstractOption)
    {
      AbstractOption option = (AbstractOption) node.elt;
      Object def = option.getValue();
      String val = prefs.get(option.getName(), def == null ? null : def.toString());
      option.setValue(val);
    }
  }

  /**
   * Get the page builder used to instantiate pages from XML descriptors.
   * @return an <code>OptionsPageBuilder</code> instance.
   */
  public static OptionsPageBuilder getBuilder()
  {
    return builder;
  }

  /**
   * Build a graph of the persistent elements.
   * @param elt the root of the current subgraph.
   * @return an <code>OptionNode</code> instance.
   */
  public static OptionNode buildPersistenceGraph(final OptionElement elt)
  {
    OptionNode node = null;
    if (elt instanceof OptionContainer)
    {
      node = new OptionNode(elt);
      OptionContainer page = (OptionContainer) elt;
      for (OptionElement child: page.getChildren())
      {
        OptionNode childNode = buildPersistenceGraph(child);
        if (childNode != null)
        {
          if (node == null) node = new OptionNode(elt);
          node.children.add(childNode);
        }
      }
    }
    else if (elt instanceof AbstractOption)
    {
      if (((AbstractOption) elt).isPersistent()) node = new OptionNode(elt);
    }
    return node;
  }

  /**
   * Get the root of the preferences subtree in which the chart configurations are saved.
   * @return a {@link Preferences} instance.
   */
  public static synchronized Preferences getPreferences()
  {
    return preferences;
  }

  /**
   * Set the root of the preferences subtree in which the chart configurations are saved.
   * @param preferences a {@link Preferences} instance.
   */
  public static synchronized void setPreferences(final Preferences preferences)
  {
    OptionsHandler.preferences = preferences;
  }

  /**
   * Load the application's main window state from the preferences store.
   * @param pref the preferences node from where the attributes are loaded.
   */
  public static void loadMainWindowAttributes(final Preferences pref)
  {
    loadFrameAttributes(mainWindow, pref);
  }

  /**
   * Save the application's main window state to the preferences store.
   * @param pref the preferences node where the attributes are saved.
   */
  public static void saveMainWindowAttributes(final Preferences pref)
  {
    saveFrameAttributes(mainWindow, pref);
  }

  /**
   * Load the specified frame state from the preferences store.
   * @param frame the frame for which the attributes are retrieved.
   * @param pref the preferences node from where the attributes are loaded.
   */
  public static void loadFrameAttributes(final Frame frame, final Preferences pref)
  {
    int x = pref.getInt("locationx", 0);
    int y = pref.getInt("locationy", 0);
    int width = pref.getInt("width", 600);
    int height = pref.getInt("height", 768);
    frame.setSize(width, height);
    frame.setLocation(x, y);
    boolean maximized = pref.getBoolean("maximized", false);
    if (maximized) frame.setExtendedState(Frame.MAXIMIZED_BOTH);
  }

  /**
   * Save the specified frame state to the preferences store.
   * @param frame the frame for which the attributes are saved.
   * @param pref the preferences node where the attributes are saved.
   */
  public static void saveFrameAttributes(final Frame frame, final Preferences pref)
  {
    int state = frame.getExtendedState();
    boolean maximized = (state & Frame.MAXIMIZED_BOTH) > 0;
    if (maximized) frame.setExtendedState(Frame.NORMAL);
    java.awt.Point p = frame.getLocation();
    pref.putInt("locationx", p.x);
    pref.putInt("locationy", p.y);
    java.awt.Dimension d = frame.getSize();
    pref.putInt("width", d.width);
    pref.putInt("height", d.height);
    pref.putBoolean("maximized", maximized);

    try
    {
      pref.flush();
    }
    catch(BackingStoreException e)
    {
    }
  }

  /**
   * A graph of the persistent options.
   */
  public static class OptionNode
  {
    /**
     * The corresponding option element.
     */
    public OptionElement elt = null;
    /**
     * The children of the corresponding option element.
     */
    public List<OptionNode> children = new ArrayList<>();

    /**
     * Initialize this node.
     * @param elt the corresponding option element.
     */
    public OptionNode(final OptionElement elt)
    {
      this.elt = elt;
    }
  }

  /**
   * Get the main window of the application.
   * @return a {@link JFrame} instance.
   */
  public static JFrame getMainWindow()
  {
    return mainWindow;
  }

  /**
   * Set the main window of the application.
   * @param mainWindow a {@link JFrame} instance.
   */
  public static void setMainWindow(final JFrame mainWindow)
  {
    OptionsHandler.mainWindow = mainWindow;
  }
}
