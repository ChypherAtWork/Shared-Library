def call(){
    def config = [:]
    //body.resolveStrategy = Closure.DELEGATE_FIRST
    //body.delegate = config
    //body()

    try {
        timeout(config.timeout ?: 180) {
            stage('Create a Version File') {
                version()
            }

            stage('Fetch Base Tag') {
                gitTagUpdate()
            }

            if (currentBuild.result == null) {
                currentBuild.result = "SUCCESS"
            }
        }
    } catch (err) {
        currentBuild.result = 'FAILURE'
        //slackNotify()
        throw err
    }
}
