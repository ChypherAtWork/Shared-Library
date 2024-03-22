def call(){
    sh"""
        echo " 
            #!/bin/bash
            # requires extended (gnu) getopt with long options
            # execute to update a version string
            # source to import version function to sourcing shell

            # error codes
            export ERROR_VERSION_STRING=1
            export ERROR_PART_FLAG=2

            # given a version string in the form 'major.minor.build' and a flag naming one of the components
            # returns an updated version string with the named component increased by 1
            # if version string given in incorrect format, returns with code ERROR_VERSION_STRING
            # if no flag or wrong flag is given, returns with error code ERROR_PART_FLAG
            version() {
                local CURRENT="\$1"
                local PART="\$2"
                grep -E '^[0-9]+\\.[0-9]+\\.[0-9]+\\$' >/dev/null <<< "\$CURRENT" || {
                    echo "wrong or no version string: '\$CURRENT'" >\&2
                    return \$ERROR_VERSION_STRING
                }
                local MAJOR=$(echo "\$CURRENT" | cut -d '.' -f1)
                local MINOR=$(echo "\$CURRENT" | cut -d '.' -f2)
                local BUILD=$(echo "\$CURRENT" | cut -d '.' -f3)
                case "\$PART" in
                    major) let MAJOR+=1;;
                    minor) let MINOR+=1;;
                    build) let BUILD+=1;;
                    *) echo "wrong or no part specification: '\$PART'" >&2; return \$ERROR_PART_FLAG;;
                esac
                echo "\$MAJOR.\$MINOR.\$BUILD"
            }

            # if script sourced, return (the version function is available in the sourcing shell)
            if [ "\$BASH_SOURCE" != "\$0" ]; then
                return
            fi

            # parse command line options
            TEMP=`getopt -o aib --long major,minor,build -n \$BASH_SOURCE -- "\$@"` || exit \$ERROR_PART_FLAG
            eval set -- "\$TEMP"

            # by default, increase the build number
            export PART='build'

            # extract options and their arguments into variables.
            while true ; do
                case "\$1" in
                    -a|--major) PART='major'; shift;;
                    -i|--minor) PART='minor'; shift;;
                    -b|--build) PART='build'; shift;;
                    --) shift; break;;
                    *) echo "wrong command line argument: '\$1'"; exit 1;;
                esac
            done

            # execute
            version "\$1" "\$PART"
        " > version.sh
        chmod u+x version.sh
    """
}
