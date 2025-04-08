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

// Nexus credentials (username and password from environment variables)
def nexusUsername = System.getenv("NEXUS_USER") ?: "nexus-user"
def nexusPassword = System.getenv("NEXUS_PASSWORD") ?: "nexus-password"

// Create a new UsernamePasswordCredentialsImpl object with the nexus credentials
def nexusCreds = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,   // Scope
    "nexus-credentials",      // Credentials ID
    "Nexus Credentials",      // Description
    nexusUsername,            // Username
    nexusPassword             // Password
)

// Add credentials to the global credentials store
credentialsStore.addCredentials(globalDomain, nexusCreds)
instance.save()

println "Credentials created successfully '${nexusUsername}' '${nexusPassword}'"