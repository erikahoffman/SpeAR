package com.rockwellcollins.spear.translate.actions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Injector;
import com.rockwellcollins.SpearInjectorUtil;
import com.rockwellcollins.spear.Constraint;
import com.rockwellcollins.spear.Definitions;
import com.rockwellcollins.spear.File;
import com.rockwellcollins.spear.FormalConstraint;
import com.rockwellcollins.spear.Specification;
import com.rockwellcollins.spear.translate.intermediate.SpearDocument;
import com.rockwellcollins.spear.translate.layout.SpearRegularLayout;
import com.rockwellcollins.spear.translate.master.SProgram;
import com.rockwellcollins.spear.translate.views.SpearEntailmentResultsView;
import com.rockwellcollins.spear.ui.preferences.PreferencesUtil;
import com.rockwellcollins.ui.internal.SpearActivator;

import jkind.api.JKindApi;
import jkind.api.results.JKindResult;
import jkind.api.results.MapRenaming;
import jkind.api.results.MapRenaming.Mode;
import jkind.api.results.Renaming;
import jkind.lustre.Program;
import jkind.results.layout.Layout;

public class CheckLogicalEntailment implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	@Override
	public void run(IAction action) {
		SpearInjectorUtil
				.setInjector(SpearActivator.getInstance().getInjector(SpearActivator.COM_ROCKWELLCOLLINS_SPEAR));

		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (!(editor instanceof XtextEditor)) {
			MessageDialog.openError(window.getShell(), "Error", "Only SpeAR files can be analyzed.");
			return;
		}

		XtextEditor xte = (XtextEditor) editor;
		IXtextDocument doc = xte.getDocument();

		doc.readOnly(new IUnitOfWork<Void, XtextResource>() {

			@Override
			public java.lang.Void exec(XtextResource state) throws Exception {
				File f = (File) state.getContents().get(0);

				Specification specification = null;
				if (f instanceof Definitions) {
					MessageDialog.openError(window.getShell(), "Error", "Cannot analyze a Definitions file.");	
				} else {
					specification = (Specification) f;
				}

				if (hasErrors(specification.eResource())) {
					MessageDialog.openError(window.getShell(), "Error", "Specification contains errors.");
					return null;
				}

				if (specification.getBehaviors().size() == 0) {
					MessageDialog.openError(window.getShell(), "Nothing to analyze",
							"The user must specify at least one property to check for logical entailment.");
					return null;
				}

				//Set the runtime options
				SpearRuntimeOptions.setRuntimeOptions();
				
				SpearDocument workingCopy = new SpearDocument(specification);
				workingCopy.transform();
				
				SProgram program = SProgram.build(workingCopy);

				Program p = program.getLogicalEntailment();
				URI lustreURI = createURI(state.getURI(), "", "lus");

				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
								
				if(SpearRuntimeOptions.printFinalLustre) {
					IResource finalResource = root.getFile(new Path(lustreURI.toPlatformString(true)));
					printResource(finalResource, p.toString());
				}

				// refresh the workspace
				root.refreshLocal(IResource.DEPTH_INFINITE, null);
				
				JKindApi api = (JKindApi) PreferencesUtil.getKindApi();
				if(SpearRuntimeOptions.enableIVCDuringEntailment) {
					api.setIvcReduction();					
				}

				Renaming renaming = new MapRenaming(workingCopy.renamed.get(workingCopy.getMain()), Mode.IDENTITY);
				List<Boolean> invert = new ArrayList<>();
				Specification s = workingCopy.specifications.get(workingCopy.mainName);
				for(Constraint c : s.getBehaviors()) {
					if (c instanceof FormalConstraint) {
						FormalConstraint fc = (FormalConstraint) c;
						if(fc.getFlagAsWitness() != null) {
							invert.add(true);
						} else {
							invert.add(false);
						}
					} else {
						invert.add(false);
					}
				}
				
				JKindResult result = new JKindResult("Spear Result", p.getMainNode().properties, invert, renaming);

				IProgressMonitor monitor = new NullProgressMonitor();
				showView(result, new SpearRegularLayout(specification));

				try {
					api.execute(p, result, monitor);
				} catch (Exception e) {
					System.out.println(result.getText());
					throw e;
				}

				return null;
			}
		});
	}

	protected boolean hasErrors(Resource res) {
		Injector injector = SpearActivator.getInstance().getInjector(SpearActivator.COM_ROCKWELLCOLLINS_SPEAR);
		IResourceValidator resourceValidator = injector.getInstance(IResourceValidator.class);

		for (Issue issue : resourceValidator.validate(res, CheckMode.ALL, CancelIndicator.NullImpl)) {
			if (issue.getSeverity() == Severity.ERROR) {
				return true;
			}
		}
		return false;
	}

	private static URI createURI(URI baseURI, String suffix, String extension) {
		String filename = baseURI.lastSegment();
		baseURI = baseURI.trimSegments(1);
		int i = filename.lastIndexOf(".");
		baseURI = baseURI.appendSegment((filename.substring(0, i) + suffix + "." + extension));
		return baseURI;
	}

	private void printResource(IResource res, String contents) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(res.getRawLocation().toFile()))) {
			bw.write(contents);
		}
	}

	private void showView(final JKindResult result, final Layout layout) {
		window.getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					SpearEntailmentResultsView page = (SpearEntailmentResultsView) window.getActivePage().showView(SpearEntailmentResultsView.ID);
					page.setInput(result, layout, null);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}

	@Override
	public void dispose() {}

	@Override
	public void init(IWorkbenchWindow arg0) {
		this.window = arg0;
	}
}
