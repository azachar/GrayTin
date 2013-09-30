package org.graytin.jenkins.update;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.graytin.jenkins.update.jobs.CheckJenkinsPeriodicallyJob;
import org.graytin.jenkins.update.svn.ISVNProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class SDKActivator extends AbstractUIPlugin implements IPropertyChangeListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.graytin.jenkins"; //$NON-NLS-1$

	// The shared instance
	private static SDKActivator plugin;

	private ISVNProvider svnProvider;

	private final CheckJenkinsPeriodicallyJob updatePeriodically = new CheckJenkinsPeriodicallyJob();

	public ISVNProvider getSvnProvider() {
		BundleContext context = getBundle().getBundleContext();
		ServiceReference<?> reference = context.getServiceReference(ISVNProvider.class);
		if (reference == null) {
			return null;
		}
		svnProvider = (ISVNProvider) context.getService(reference);
		return svnProvider;
	}

	/**
	 * The constructor
	 */
	public SDKActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		getPreferenceStore().addPropertyChangeListener(this);
		if (getPreferenceStore().getBoolean(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_PERIODICALLY)){
			updatePeriodically.schedule();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_PERIODICALLY) || event.getProperty().equals(SDKBundleUpdatePreferencePage.DO_SCAN_BUILDS_EVERY)){
			updatePeriodically.cancel();

			if (Boolean.TRUE.equals(event.getNewValue())){
				updatePeriodically.restart();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static IFolder getSDKBundleFolder() {
		String folder = getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.SDK_BUNDLES_FOLDER);
		return (IFolder) ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(folder));
	}
	public static boolean isSDKStyle() {
		return getSDKBundleFolder().isAccessible();
	}
	
	public static IFile getTargetPlatformFile() {
		String file = getDefault().getPreferenceStore().getString(SDKBundleUpdatePreferencePage.SDK_TARGET_PLATFORM_FILE);
		return (IFile) ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(file));
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SDKActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	
}
