package ca.uvic.cs.tagsea.statistics.svn;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SVNStatistics extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ca.uvic.cs.tagsea.statistics.svn";

	// The shared instance
	private static SVNStatistics plugin;
	
	/**
	 * The constructor
	 */
	public SVNStatistics() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SVNStatistics getDefault() {
		return plugin;
	}

}
