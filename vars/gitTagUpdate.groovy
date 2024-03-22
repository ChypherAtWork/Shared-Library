def call(){
    sh """
        #!/bin/bash
        set +x
        err="Please create a new release tag first in the below format.
            For development specific branch,
            i.e. dev -> 1.0.0+dev or future -> 1.0.0+dev
            For qa specific branch,
            i.e. qa -> 1.0.0+qa
            For production branch,
            i.e. master -> 1.0.0 or main -> 1.0.0

            Create a new release tag only when a minor change is being developed,
            for dev  -> 1.1.0+dev
                qa   -> 1.1.0+qa
                main -> 1.1.0

            Create a new release tag if a (new AI module) major change is being developed,
            for dev  -> 2.0.0+dev
                qa   -> 2.0.0+qa
                main -> 2.0.0
            "
        git remote remove origin && git remote add origin '${env.GIT_URL}'
        set -xv;
        case "${env.BRANCH_NAME}" in
            future) postfix="[0-9]+\\.[0-9]+\\.[0-9]\\+dev";;
            dev) postfix="[0-9]+\\.[0-9]+\\.[0-9]\\+dev";;
            qa) postfix="[0-9]+\\.[0-9]+\\.[0-9]\\+qa";;
            master) postfix="[0-9]+\\.[0-9]+\\.[0-9]+\$";;
            main) postfix="[0-9]+\\.[0-9]+\\.[0-9]+\$";;
        esac
        base=\$("git tag")
        # Fetch the base release tag related to the current working branch
        BASE_TAG=`git tag | grep -E "\$postfix" | sort -V | tail -n 1`
        # BASE_TAG=`git describe --match "\$postfix" --tags --abbrev=0 2>/dev/null || true`

        if [ -z "\$BASE_TAG" ]; then
            echo "${env.GIT_URL}"
            echo "${env.BRANCH_NAME}"
            echo -e "\$err"
            exit 1
        else
            # Split the tag 1.0.0+dev=1.0.0 and  +dev
            SEMVER=`echo "\${BASE_TAG}" | grep -o '^[0-9]\\+\\.[0-9]\\+\\.[0-9]\\+'`
            ENV=`echo "\$BASE_TAG" | grep -o '+.*' || true`

            # Update and increase patch 1.0.1 tag
            NEW_VERSION=`./version.sh \$SEMVER`

            # Merge new updated tag + env 1.0.1+dev
            NEW_GIT_RELEASE_TAG="\$NEW_VERSION\$ENV"
            echo "New release tag : \$NEW_GIT_RELEASE_TAG"
            git tag \$NEW_GIT_RELEASE_TAG

            # Fail the job if manually build from the jenkins console
            if [ "\$(git rev-list -n 1 "\$BASE_TAG")" = "\$(git rev-list -n 1 "\$NEW_GIT_RELEASE_TAG")" ]; then
                echo 'This job cannot be run manually to avoid version conflicts. It will be executed automatically only when new changes/PR merged into the same branch.'
                exit 1
            fi

            git push origin \$NEW_GIT_RELEASE_TAG
        fi
        pip install twine && pip install setuptools==61.2.0 && python -m pip install wheel > /dev/null
        export AI_DATA_VERSION=\$NEW_GIT_RELEASE_TAG
        python setup.py bdist_wheel
        python -m twine upload --skip-existing dist/* -u ${PYPI_USR} -p ${PYPI_PSW} --repository-url ${PYPI_REPO} --verbose
    """
}
