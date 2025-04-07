import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*

def jenkins = Jenkins.getInstance()
def folderName = System.getenv("JENKINS_FOLDER") ?: "dagger"

// Check if the folder already exists
if (jenkins.getItem(folderName) == null) {
    // Create a new folder
    def folder = new Folder(jenkins, folderName)

    // Add the folder to Jenkins
    jenkins.add(folder, folderName)

    // Save the Jenkins configuration to reflect the changes
    jenkins.save()

    println "Folder '${folderName}' created."
} else {
    println "Folder '${folderName}' already exists."
}