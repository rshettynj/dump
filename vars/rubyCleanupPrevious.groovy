#!/usr/bin/groovy
/* Usage:
  rubyCleanupPrevious {
    // rubyScript is optional parameter
    rubyScript       = '/usr/local/bin/unixStackUtils.rb'
    cfTemplateName   = 'project_template_stack.template'
    cfTemplatePrefix = 'projA'
    publicBucket     = 'mypublicbucket'
  }
*/
def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  
  // get environment variables
  def env = System.getenv()

  def rubyScript = config.rubyScript ?: env['UNIXSTACKUTILS']
  def cfTemplateName = config.cfTemplateName
  def cfTemplatePrefix = config.cfTemplatePrefix
  def publicBucket = config.publicBucket

  sh "ruby ${rubyScript} deleteallbyprefixwithartifactsnowaitfromdev ${cfTemplateName} ${cfTemplatePrefix} ${publicBucket}"

}
