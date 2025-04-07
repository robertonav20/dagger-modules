import jenkins.model.*
import hudson.security.*
import hudson.util.Secret
import com.cloudbees.hudson.plugins.folder.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*

def instance = Jenkins.getInstance()
def globalDomain = Domain.global()
def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]?.getStore()

// Gitea credentials (username and password from environment variables)
def giteaUsername = System.getenv("GITEA_USER") ?: "gitea-user"
def giteaPassword = System.getenv("GITEA_PASSWORD") ?: "gitea-password"

// Create a new UsernamePasswordCredentialsImpl object with the Gitea credentials
def giteaCreds = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,   // Scope
    "gitea-credentials",      // Credentials ID
    "Gitea Credentials",      // Description
    giteaUsername,            // Username
    giteaPassword             // Password
)

// Add credentials to the global credentials store
credentialsStore.addCredentials(globalDomain, giteaCreds)
instance.save()

println "Credentials created successfully '${giteaUsername}' '${giteaPassword}'"