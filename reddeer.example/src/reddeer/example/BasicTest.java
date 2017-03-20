package reddeer.example;

import static org.junit.Assert.*;

import java.util.List;

import org.jboss.reddeer.common.matcher.RegexMatcher;
import org.jboss.reddeer.common.wait.WaitUntil;
import org.jboss.reddeer.common.wait.WaitWhile;
import org.jboss.reddeer.core.matcher.WithTextMatcher;
import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
import org.jboss.reddeer.eclipse.jdt.ui.wizards.JavaProjectWizard;
import org.jboss.reddeer.eclipse.jdt.ui.wizards.NewClassCreationWizard;
import org.jboss.reddeer.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.jboss.reddeer.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
import org.jboss.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.jboss.reddeer.eclipse.ui.views.log.LogMessage;
import org.jboss.reddeer.eclipse.ui.views.log.LogView;
import org.jboss.reddeer.junit.runner.RedDeerSuite;
import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.workbench.condition.ViewIsOpen;
import org.jboss.reddeer.workbench.core.condition.JobIsRunning;
import org.jboss.reddeer.workbench.impl.editor.TextEditor;
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
		NewJavaProjectWizardPageOne projectFirstPage = new NewJavaProjectWizardPageOne();
		projectFirstPage.setProjectName(PROJECT_NAME);
		projectWizard.finish();
		
		ProjectExplorer projectExplorer = new ProjectExplorer();
		projectExplorer.open();
		assertTrue(projectExplorer.containsProject(PROJECT_NAME));
		
		NewClassCreationWizard classWizard = new NewClassCreationWizard();
		classWizard.open();
		NewClassWizardPage classFirstPage = new NewClassWizardPage();
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
		new ContextMenu(new WithTextMatcher("Run As"), new WithTextMatcher(new RegexMatcher(".*Java Application.*"))).select();
		new WaitWhile(new JobIsRunning());
		
		new WaitUntil(new ViewIsOpen(new ConsoleView()));
		new WaitUntil(new ConsoleHasText(CONSOLE_TEXT));
		
		errorLog.open();
		checkLogMessages(errorLog.getErrorMessages());
		checkLogMessages(errorLog.getWarningMessages());
	}
	
	private void checkLogMessages(List<LogMessage> messages){
		if(messages.size() == 1){
			//this should be filtered by RedDeer. https://github.com/jboss-reddeer/reddeer/issues/1617
			if(!messages.get(0).getMessage().contains("No log entry found within maximum log size")){
				fail("Error log contains message '"+messages.get(0).getMessage()+"'");
			}
		} else {
			assertTrue(messages.size() == 0);
		}
		
	}

}