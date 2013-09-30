package org.graytin.jenkins.update.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEPreferencesManager;
import org.eclipse.pde.internal.launching.launcher.BundleLauncherHelper;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.graytin.jenkins.update.dialog.Messages;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Re-open download SVN connectors dialog
 * 
 * @author Andrej Zachar
 */
public class UpdateLaunchConfiguration extends AbstractHandler {

	private static final String STUDIO_PRODUCT = "studio.product";

	private IPluginModelBase[] fWorkspaceModels;

	private IPluginModelBase[] fExternalModels;

	/**
	 * Returns an array of plugins from the workspace. Non-OSGi plugins (no valid bundle manifest) will be filtered out.
	 * 
	 * @return array of workspace OSGi plugins, possibly empty
	 */
	protected IPluginModelBase[] getWorkspaceModels() {
		if (fWorkspaceModels == null) {
			IPluginModelBase[] models = PluginRegistry.getWorkspaceModels();
			ArrayList<IPluginModelBase> list = new ArrayList<IPluginModelBase>(models.length);
			for (int i = 0; i < models.length; i++) {
				if (models[i].getBundleDescription() != null) {
					list.add(models[i]);
				}
			}
			fWorkspaceModels = list.toArray(new IPluginModelBase[list.size()]);
		}
		return fWorkspaceModels;
	}

	/**
	 * Returns an array of external plugins that are currently enabled.
	 * 
	 * @return array of external enabled plugins, possibly empty
	 */
	@SuppressWarnings("restriction")
	protected IPluginModelBase[] getExternalModels() {
		if (fExternalModels == null) {
			PDEPreferencesManager pref = PDECore.getDefault().getPreferencesManager();
			String saved = pref.getString(ICoreConstants.CHECKED_PLUGINS);
			if (saved.equals(ICoreConstants.VALUE_SAVED_NONE)) {
				fExternalModels = new IPluginModelBase[0];
				return fExternalModels;
			}

			IPluginModelBase[] models = PluginRegistry.getExternalModels();
			if (saved.equals(ICoreConstants.VALUE_SAVED_ALL)) {
				fExternalModels = models;
				return fExternalModels;
			}

			ArrayList<IPluginModelBase> list = new ArrayList<IPluginModelBase>(models.length);
			for (int i = 0; i < models.length; i++) {
				if (models[i].isEnabled()) {
					list.add(models[i]);
				}
			}
			fExternalModels = list.toArray(new IPluginModelBase[list.size()]);
		}
		return fExternalModels;
	}
//TODO aza BundleLauncherHelper
	class PluginModelNameBuffer {
		private List<String> nameList;

		PluginModelNameBuffer() {
			super();
			nameList = new ArrayList<String>();
		}

		void add(IPluginModelBase model) {
			nameList.add(getPluginName(model));
		}

		@SuppressWarnings("restriction")
		private String getPluginName(IPluginModelBase model) {
			String startLevel = null;
			String autoStart = "true";
			//TODO 
			//				startLevel = levelColumnCache.get(model) != null ? levelColumnCache.get(model).toString() : null;
			//				autoStart = autoColumnCache.get(model) != null ? autoColumnCache.get(model).toString() : null;
			return BundleLauncherHelper.writeBundleEntry(model, startLevel, autoStart);
		}

		public String toString() {
			Collections.sort(nameList);
			StringBuffer result = new StringBuffer();
			for (Iterator<String> iterator = nameList.iterator(); iterator.hasNext();) {
				String name = iterator.next();
				if (result.length() > 0)
					result.append(',');
				result.append(name);
			}

			if (result.length() == 0)
				return null;

			return result.toString();
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ILaunchConfiguration conf = findConfiguration(STUDIO_PRODUCT);
		if (conf == null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), NLS.bind(Messages.UpdateLaunchConfiguration_1, STUDIO_PRODUCT),
							NLS.bind(Messages.UpdateLaunchConfiguration_2, STUDIO_PRODUCT));
				}
			});
			return null;
		}
		try {
			final ILaunchConfigurationWorkingCopy config = conf.getWorkingCopy();

			PluginModelNameBuffer wBuffer = new PluginModelNameBuffer();
//			PluginModelNameBuffer tBuffer = new PluginModelNameBuffer();

			// default the checkstate to all workspace plug-ins
			TreeSet<String> checkedWorkspace = new TreeSet<String>();
			IPluginModelBase[] workspaceModels = getWorkspaceModels();
			for (int i = 0; i < workspaceModels.length; i++) {
				IPluginModelBase model = workspaceModels[i];
				String id = model.getPluginBase().getId();
				//do not enable fragments
				if (id != null && !model.isFragmentModel()) {
					wBuffer.add(model);
					checkedWorkspace.add(id);
				}
			}

//			IPluginModelBase[] externalModels = getExternalModels();
//			for (int i = 0; i < externalModels.length; i++) {
//				IPluginModelBase model = externalModels[i];
//				// If there is a workspace bundle with the same id, don't check the external version
//				if (!checkedWorkspace.contains(model.getPluginBase().getId()) && model.isEnabled()) {
//					tBuffer.add(model);
//				}
//			}

			final String oldWorkspace = config.getAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, "");//$NON-NLS-2$
//			String oldTarget = config.getAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, "");//$NON-NLS-2$

			config.setAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, wBuffer.toString());
//			config.setAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, tBuffer.toString());

			final String newWorkspace = config.getAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, "");//$NON-NLS-2$
//			final String newTarget = config.getAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, "");//$NON-NLS-2$
			
			
			final boolean same = newWorkspace.equals(oldWorkspace);

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (same) {
						MessageDialog.openInformation(Display.getCurrent().getActiveShell(), NLS.bind(Messages.UpdateLaunchConfiguration_19, STUDIO_PRODUCT),
								NLS.bind(Messages.UpdateLaunchConfiguration_20, STUDIO_PRODUCT));
						return;
					}
					
					CompareConfiguration cc = new CompareConfiguration();
					cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
					StringCompare input = new StringCompare(cc, oldWorkspace.replaceAll(",", "\n"), newWorkspace.replaceAll(",", "\n"));
					CompareUI.openCompareEditorOnPage(input, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
					
					
					boolean doUpdate = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
							NLS.bind(Messages.UpdateLaunchConfiguration_21, STUDIO_PRODUCT),
							NLS.bind(Messages.UpdateLaunchConfiguration_22, oldWorkspace, newWorkspace)); //$NON-NLS-2$
					if (doUpdate) {

						IRunnableWithProgress runnable = new IRunnableWithProgress() {
							@SuppressWarnings("restriction")
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
									monitor.beginTask("", 2);
									String time = new DateTime().toString(DateTimeFormat.shortTime());
									time = time.replace(":", "-");
									ILaunchConfigurationWorkingCopy backup = conf.copy(conf.getName() + " backup " + time); //$NON-NLS-1$
									((LaunchConfigurationWorkingCopy) config).doSave(new SubProgressMonitor(monitor, 1));
									((LaunchConfigurationWorkingCopy) backup).doSave(new SubProgressMonitor(monitor, 1));
								} catch (CoreException e) {
									e.printStackTrace();
								} finally {
									monitor.done();
								}
							}
						};
						try {
							PlatformUI.getWorkbench().getProgressService().run(true, false, runnable);
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});

		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ILaunchConfiguration findConfiguration(String name) {

		try {

			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType[] types = manager.getLaunchConfigurationTypes();

			for (int i = 0; i < types.length; i++) {
				ILaunchConfigurationType type = types[i];
				ILaunchConfiguration[] configurations;
				configurations = manager.getLaunchConfigurations(type);
				for (int j = 0; j < configurations.length; j++) {
					ILaunchConfiguration configuration = configurations[j];
					if (name.equals(configuration.getName())) {
						return configuration;
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}

//		PluginModelNameBuffer buffer = new PluginModelNameBuffer();
//		if (fAddWorkspaceButton.getSelection()) {
//			IPluginModelBase[] workspaceModels = getWorkspaceModels();
//			for (int i = 0; i < workspaceModels.length; i++) {
//				if (!fPluginTreeViewer.getChecked(workspaceModels[i])) {
//					buffer.add(workspaceModels[i]);
//				}
//			}
//		}
//		config.setAttribute(IPDELauncherConstants.DESELECTED_WORKSPACE_PLUGINS, buffer.toString());
//			

//			
//			
//			
//			
//			
//
//			String targetPlatformString = wc.getAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, ""); //$NON-NLS-1$
//			String[] targetPlatformPlugins = StringUtils.split(targetPlatformString, ","); //$NON-NLS-1$
//
//			String workspaceString = wc.getAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, ""); //$NON-NLS-1$
//			String[] workspaceStringPlugins = StringUtils.split(workspaceString, ","); //$NON-NLS-1$
//
//			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
//
//			for (IProject project : projects) {
//				IPluginModelBase pluginProject = PluginRegistry.findModel(project);
//				if (pluginProject == null) {
//					continue; //skip no plugin project
//				}
//				boolean isFragment = pluginProject.getPluginBase().getPluginModel().getBundleDescription().getHost() != null;
//				if (isFragment) {
//					continue; //fragment bundles are ignored
//				}
//				String symbolicName = pluginProject.getPluginBase().getPluginModel().getBundleDescription().getSymbolicName();
//				if (symbolicName.contains("unittest")) { //$NON-NLS-1$
//					continue; //test bundles are ignored
//				}
//
//				if (targetPlatformString.contains(symbolicName)) {
//					for (String targetPluginDef : targetPlatformPlugins) {
//						if (targetPluginDef.contains(symbolicName)) {
//							//remove from target platform
//							targetPlatformString.replaceAll(targetPluginDef, ""); //$NON-NLS-1$
//							//ensure it is in workspace enabled
//							if (workspaceString.contains(symbolicName)) {
//								changes.add(NLS.bind(Messages.UpdateLaunchConfiguration_9, targetPluginDef));
//							} else {
//								changes.add(NLS.bind(Messages.UpdateLaunchConfiguration_10, targetPluginDef));
//								workspaceString += targetPluginDef;
//							}
//						}
//					}
//				}
//				String newStartDefinition = symbolicName + "@default:true"; //$NON-NLS-1$
//				if (workspaceString.contains(symbolicName)) {
//					//ensure that is enabled
//					for (String workspacePluginDef : workspaceStringPlugins) {
//						if (workspacePluginDef.contains(symbolicName)) {
//							if (!workspacePluginDef.endsWith("true")) { //$NON-NLS-1$
//								changes.add(NLS.bind(Messages.UpdateLaunchConfiguration_13, newStartDefinition));
//								workspaceString.replaceAll(workspacePluginDef, newStartDefinition);
//							}
//						}
//					}
//				} else {
//					changes.add(NLS.bind(Messages.UpdateLaunchConfiguration_11, newStartDefinition));
//					workspaceString += newStartDefinition + ","; //$NON-NLS-1$
//				}
//			}
//			workspaceString = StringUtils.removeEnd(workspaceString, ",");//$NON-NLS-1$
//			wc.setAttribute(IPDELauncherConstants.SELECTED_TARGET_PLUGINS, targetPlatformString); //$NON-NLS-1$
//			wc.setAttribute(IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, workspaceString); //$NON-NLS-1$
//
//
//Display.getDefault().syncExec(new Runnable() {
//	public void run() {
//		if (changes.isEmpty()) {
//			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), NLS.bind(Messages.UpdateLaunchConfiguration_19, STUDIO_PRODUCT),
//					NLS.bind(Messages.UpdateLaunchConfiguration_20, STUDIO_PRODUCT));
//			return;
//		}
//		boolean ok = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
//				NLS.bind(Messages.UpdateLaunchConfiguration_21, STUDIO_PRODUCT),
//				NLS.bind(Messages.UpdateLaunchConfiguration_22, changes, STUDIO_PRODUCT)); //$NON-NLS-2$
//		if (ok) {
//
//			IRunnableWithProgress runnable = new IRunnableWithProgress() {
//				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					try {
//						monitor.beginTask("", 2);
//						ILaunchConfigurationWorkingCopy backup = conf.copy(conf.getName() + " backup"); //$NON-NLS-1$
//						((LaunchConfigurationWorkingCopy) config).doSave(new SubProgressMonitor(monitor, 1));
//						((LaunchConfigurationWorkingCopy) backup).doSave(new SubProgressMonitor(monitor, 1));
//					} catch (CoreException e) {
//						DebugUIPlugin.log(e);
//					} finally {
//						monitor.done();
//					}
//				}
//			};
//		}
//	}
//});
