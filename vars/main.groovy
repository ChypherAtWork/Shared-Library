package com.ansible

def callTestScript() {
    def main = load('./test.groovy')
    main()
}
