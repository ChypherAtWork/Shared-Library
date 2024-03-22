package com.example

def callTestScript() {
    def testScript = load 'vars/test.groovy'
    testScript.call()
}
