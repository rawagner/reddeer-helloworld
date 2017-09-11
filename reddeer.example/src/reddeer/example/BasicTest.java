package reddeer.example;

import static org.junit.Assert.*;

import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.JavaProjectWizard;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.NewClassCreationWizard;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.reddeer.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.eclipse.ui.views.log.LogView;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.workbench.condition.ViewIsOpen;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@CleanWorkspace
@RunWith(RedDeerSuite.class)
public class BasicTest {
	
	private static final String PROJECT_NAME="JavaProject";
	private static final String CLASS_NAME="MyTestClass";
	private static final String CLASS_PACKAGE="org.eclipse.test";
	private static final String CONSOLE_TEXT="Hello RedDeer!";
	private static LogView errorLog = new LogView();
	
	@BeforeClass
	public static void deleteErrorLog(){
		errorLog.open();
		errorLog.deleteLog();
	}
	
	@Test
	public void test(){
		JavaProjectWizard projectWizard = new JavaProjectWizard();
		projectWizard.open();
		NewJavaProjectWizardPageOne projectFirstPage = new NewJavaProjectWizardPageOne(projectWizard);
		projectFirstPage.setProjectName(PROJECT_NAME);
		projectWizard.finish();
		
		ProjectExplorer projectExplorer = new ProjectExplorer();
		projectExplorer.open();
		assertTrue(projectExplorer.containsProject(PROJECT_NAME));
		
		NewClassCreationWizard classWizard = new NewClassCreationWizard();
		classWizard.open();
		NewClassWizardPage classFirstPage = new NewClassWizardPage(classWizard);
		classFirstPage.setName(CLASS_NAME);
		classFirstPage.setPackage(CLASS_PACKAGE);
		classFirstPage.setStaticMainMethod(true);
		classWizard.finish();
		
		assertTrue(projectExplorer.getProject(PROJECT_NAME).containsResource("src",CLASS_PACKAGE,CLASS_NAME+".java"));
		projectExplorer.getProject(PROJECT_NAME).getProjectItem("src",CLASS_PACKAGE,CLASS_NAME+".java").open();
		
		TextEditor textEditor = new TextEditor(CLASS_NAME+".java");
		int psvmLine = textEditor.getLineOfText("public static void main");
		assertTrue(psvmLine != -1);
		textEditor.insertLine(psvmLine+1, "System.out.println(\""+CONSOLE_TEXT+"\");");
		textEditor.save();
		
		projectExplorer.getProject(PROJECT_NAME).select();
		new ContextMenuItem(new WithTextMatcher("Run As"), new WithTextMatcher(new RegexMatcher(".*Java Application.*"))).select();
		
		new WaitUntil(new ViewIsOpen(new ConsoleView()));
		new WaitUntil(new ConsoleHasText(CONSOLE_TEXT));
		
		errorLog.open();
		assertTrue(errorLog.getErrorMessages().size() == 0);
		assertTrue(errorLog.getWarningMessages().size() == 0);
	}
}