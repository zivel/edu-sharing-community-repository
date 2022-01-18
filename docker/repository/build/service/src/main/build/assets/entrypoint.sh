#!/bin/bash
set -eux

########################################################################################################################

my_admin_pass="${REPOSITORY_SERVICE_ADMIN_PASS:-admin}"
my_admin_pass_md4="$(printf '%s' "$my_admin_pass" | iconv -t utf16le | openssl md4 | awk '{ print $2 }')"

my_guest_user="${REPOSITORY_SERVICE_GUEST_USER:-}"
my_guest_pass="${REPOSITORY_SERVICE_GUEST_PASS:-}"

my_bind="${REPOSITORY_SERVICE_BIND:-"0.0.0.0"}"

my_home_appid="${REPOSITORY_SERVICE_HOME_APPID:-local}"
my_home_auth="${REPOSITORY_SERVICE_HOME_AUTH:-}"
my_home_provider="${REPOSITORY_SERVICE_HOME_PROVIDER:-}"

my_prot_external="${REPOSITORY_SERVICE_PROT_EXTERNAL:-http}"
my_host_external="${REPOSITORY_SERVICE_HOST_EXTERNAL:-repository.127.0.0.1.nip.io}"
my_port_external="${REPOSITORY_SERVICE_PORT_EXTERNAL:-8100}"
my_path_external="${REPOSITORY_SERVICE_PATH_EXTERNAL:-/edu-sharing}"
my_base_external="${my_prot_external}://${my_host_external}:${my_port_external}${my_path_external}"
my_auth_external="${my_base_external}/services/authentication"
my_pool_external="${REPOSITORY_SERVICE_POOL_EXTERNAL:-200}"
my_wait_external="${REPOSITORY_SERVICE_WAIT_EXTERNAL:--1}"

my_host_internal="${REPOSITORY_SERVICE_HOST_INTERNAL:-repository-service}"
my_port_internal="${REPOSITORY_SERVICE_PORT_INTERNAL:-8080}"
my_pool_internal="${REPOSITORY_SERVICE_POOL_INTERNAL:-200}"
my_wait_internal="${REPOSITORY_SERVICE_WAIT_INTERNAL:--1}"

my_session_timeout="${REPOSITORY_SERVICE_HTTP_SESSION_TIMEOUT:-60}"

repository_cache_host="${REPOSITORY_CACHE_HOST:-}"
repository_cache_port="${REPOSITORY_CACHE_PORT:-}"

repository_database_driv="${REPOSITORY_DATABASE_DRIV:-"org.postgresql.Driver"}"
repository_database_host="${REPOSITORY_DATABASE_HOST:-repository-database}"
repository_database_name="${REPOSITORY_DATABASE_NAME:-repository}"
repository_database_opts="${REPOSITORY_DATABASE_OPTS:-}"
repository_database_pass="${REPOSITORY_DATABASE_PASS:-repository}"
repository_database_pool_max="${REPOSITORY_DATABASE_POOL_MAX:-80}"
repository_database_pool_sql="${REPOSITORY_DATABASE_POOL_SQL:-SELECT 1}"
repository_database_port="${REPOSITORY_DATABASE_PORT:-5432}"
repository_database_prot="${REPOSITORY_DATABASE_PROT:-"postgresql"}"
repository_database_user="${REPOSITORY_DATABASE_USER:-repository}"
repository_database_jdbc="jdbc:${repository_database_prot}://${repository_database_host}:${repository_database_port}/${repository_database_name}${repository_database_opts}"

repository_httpclient_disablesni4hosts="${REPOSITORY_SERVICE_HTTP_CLIENT_DISABLE_SNI4HOSTS:-}"
repository_httpclient_proxy_host="${REPOSITORY_SERVICE_HTTP_CLIENT_PROXY_HOST:-}"
repository_httpclient_proxy_nonproxyhosts="${REPOSITORY_SERVICE_HTTP_CLIENT_PROXY_NONPROXYHOSTS:-}"
repository_httpclient_proxy_proxyhost="${REPOSITORY_SERVICE_HTTP_CLIENT_PROXY_PROXYHOST:-}"
repository_httpclient_proxy_proxypass="${REPOSITORY_SERVICE_HTTP_CLIENT_PROXY_PROXYPASS:-}"
repository_httpclient_proxy_proxyport="${REPOSITORY_SERVICE_HTTP_CLIENT_PROXY_PROXYPORT:-}"
repository_httpclient_proxy_proxyuser="${REPOSITORY_SERVICE_HTTP_CLIENT_PROXY_PROXYUSER:-}"

repository_search_solr4_host="${REPOSITORY_SEARCH_SOLR4_HOST:-repository-search-solr4}"
repository_search_solr4_port="${REPOSITORY_SEARCH_SOLR4_PORT:-8080}"

repository_transform_host="${REPOSITORY_TRANSFORM_HOST:-}"
repository_transform_port="${REPOSITORY_TRANSFORM_PORT:-}"

catSConf="tomcat/conf/server.xml"
catCConf="tomcat/conf/Catalina/localhost/edu-sharing.xml"
catWConf="tomcat/webapps/edu-sharing/WEB-INF/web.xml"

eduCConf="tomcat/shared/classes/config/defaults/client.config.xml"

alfProps="tomcat/shared/classes/config/cluster/alfresco-global.properties"
eduSConf="tomcat/shared/classes/config/cluster/edu-sharing.deployment.conf"
homeProp="tomcat/shared/classes/config/cluster/applications/homeApplication.properties.xml"

### Wait ###############################################################################################################

[[ -n "${repository_cache_host}" && -n "${repository_cache_port}" ]] && {

	until wait-for-it "${repository_cache_host}:${repository_cache_port}" -t 3; do sleep 1; done

	grep -Fq 'clusterServersConfig' tomcat/conf/redisson.yaml && {
		until [[ $(redis-cli --cluster info "${repository_cache_host}" "${repository_cache_port}" | grep '[OK]' | cut -d ' ' -f5) -gt 1 ]]; do
			echo >&2 "Waiting for ${repository_cache_host} ..."
			sleep 3
		done
	}

}

until wait-for-it "${repository_database_host}:${repository_database_port}" -t 3; do sleep 1; done

until PGPASSWORD="${repository_database_pass}" \
	psql -h "${repository_database_host}" -p "${repository_database_port}" -U "${repository_database_user}" -d "${repository_database_name}" -c '\q'; do
	echo >&2 "Waiting for ${repository_database_host} ..."
	sleep 3
done

[[ -n "${repository_transform_host}" && -n "${repository_transform_port}" ]] && {
	until wait-for-it "${repository_transform_host}:${repository_transform_port}" -t 3; do sleep 1; done
}

### config #############################################################################################################

configs=(defaults plugins cluster node)

for config in "${configs[@]}"; do
	if [[ ! -f tomcat/shared/classes/config/$config/version.json ]]; then
		mkdir -p tomcat/shared/classes/config/$config
		for jar in tomcat/shared/lib/$config/*.jar; do
			unzip -o $jar -d tomcat/shared/classes/config/$config -x 'META-INF/*'
			rm $jar
		done
		cp -f tomcat/webapps/edu-sharing/version.json tomcat/shared/classes/config/$config
	else
		cmp -s tomcat/webapps/edu-sharing/version.json tomcat/shared/classes/config/$config/version.json || {
			mv tomcat/shared/classes/config/$config/version.json tomcat/shared/classes/config/$config/version.json.$(date +%d-%m-%Y_%H-%M-%S )
			cp tomcat/webapps/edu-sharing/version.json tomcat/shared/classes/config/$config/version.json
		}
	fi
done

reinstall.sh

########################################################################################################################

export CATALINA_OUT="/dev/stdout"

export CATALINA_OPTS="-Dfile.encoding=UTF-8 $CATALINA_OPTS"
export CATALINA_OPTS="-Duser.country=DE $CATALINA_OPTS"
export CATALINA_OPTS="-Duser.language=de $CATALINA_OPTS"

export CATALINA_OPTS="-Dorg.xml.sax.parser=com.sun.org.apache.xerces.internal.parsers.SAXParser $CATALINA_OPTS"
export CATALINA_OPTS="-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl $CATALINA_OPTS"
export CATALINA_OPTS="-Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl $CATALINA_OPTS"

xmlstarlet ed -L \
	-d '/Server/Service[@name="Catalina"]/Connector' \
	-s '/Server/Service[@name="Catalina"]' -t elem -n 'Connector' -v '' \
	--var internal '$prev' \
	-i '$internal' -t attr -n "address" -v "${my_bind}" \
	-i '$internal' -t attr -n "port" -v "8080" \
	-i '$internal' -t attr -n "scheme" -v "http" \
	-i '$internal' -t attr -n "proxyName" -v "${my_host_internal}" \
	-i '$internal' -t attr -n "proxyPort" -v "${my_port_internal}" \
	-i '$internal' -t attr -n "protocol" -v "org.apache.coyote.http11.Http11NioProtocol" \
	-i '$internal' -t attr -n "connectionTimeout" -v "${my_wait_internal}" \
	-i '$internal' -t attr -n "maxThreads" -v "${my_pool_internal}" \
	-s '/Server/Service[@name="Catalina"]' -t elem -n 'Connector' -v '' \
	--var external '$prev' \
	-i '$external' -t attr -n "address" -v "${my_bind}" \
	-i '$external' -t attr -n "port" -v "8081" \
	-i '$external' -t attr -n "scheme" -v "${my_prot_external}" \
	-i '$external' -t attr -n "proxyName" -v "${my_host_external}" \
	-i '$external' -t attr -n "proxyPort" -v "${my_port_external}" \
	-i '$external' -t attr -n "protocol" -v "org.apache.coyote.http11.Http11NioProtocol" \
	-i '$external' -t attr -n "connectionTimeout" -v "${my_wait_external}" \
	-i '$external' -t attr -n "maxThreads" -v "${my_pool_external}" \
	${catSConf}

[[ -n "${repository_cache_host}" && -n "${repository_cache_port}" ]] && {
	xmlstarlet ed -L \
		-d '/Context/Manager[@className="org.redisson.tomcat.RedissonSessionManager"]' \
		-s '/Context' -t elem -n "Manager" -v "" \
		--var redis '$prev' \
		-i '$redis' -t attr -n "className" -v "org.redisson.tomcat.RedissonSessionManager" \
		-i '$redis' -t attr -n "configPath" -v "tomcat/conf/redisson.yaml" \
		-i '$redis' -t attr -n "readMode" -v "MEMORY" \
		-i '$redis' -t attr -n "updateMode" -v "AFTER_REQUEST" \
		-i '$redis' -t attr -n "broadcastSessionEvents" -v "true" \
		-i '$redis' -t attr -n "broadcastSessionUpdates" -v "true" \
		${catCConf}
}

xmlstarlet ed -L \
  -N x="http://java.sun.com/xml/ns/javaee" \
	-u '/x:web-app/x:session-config/x:session-timeout' -v "${my_session_timeout}" \
	${catWConf}

### Alfresco platform ##################################################################################################

sed -i -r 's|^[#]*\s*dir\.root=.*|dir.root='"$ALF_HOME/alf_data"'|' "${alfProps}"
grep -q '^[#]*\s*dir\.root=' "${alfProps}" || echo "dir.root=$ALF_HOME/alf_data" >>"${alfProps}"

sed -i -r 's|^[#]*\s*dir\.keystore=.*|dir.keystore='"$ALF_HOME/keystore"'|' "${alfProps}"
grep -q '^[#]*\s*dir\.keystore=' "${alfProps}" || echo "dir.keystore=$ALF_HOME/keystore" >>"${alfProps}"

sed -i -r 's|^[#]*\s*img\.root=.*|img.root=/usr|' "${alfProps}"
grep -q '^[#]*\s*img\.root=' "${alfProps}" || echo 'img.root=/usr' >>"${alfProps}"

sed -i -r 's|^[#]*\s*img\.gslib=.*|img.gslib=/usr/bin|' "${alfProps}"
grep -q '^[#]*\s*img\.gslib=' "${alfProps}" || echo 'img.gslib=/usr/bin' >>"${alfProps}"

sed -i -r 's|^[#]*\s*exiftool\.dyn=.*|exiftool.dyn=/usr/bin|' "${alfProps}"
grep -q '^[#]*\s*exiftool\.dyn=' "${alfProps}" || echo 'exiftool.dyn=/usr/bin' >>"${alfProps}"

sed -i -r 's|^[#]*\s*exiftool\.exe=.*|exiftool.exe=${exiftool.dyn}/exiftool|' "${alfProps}"
grep -q '^[#]*\s*exiftool\.exe=' "${alfProps}" || echo 'exiftool.exe=${exiftool.dyn}/exiftool' >>"${alfProps}"

sed -i -r 's|^[#]*\s*ffmpeg\.dyn=.*|ffmpeg.dyn=/usr/bin|' "${alfProps}"
grep -q '^[#]*\s*ffmpeg\.dyn=' "${alfProps}" || echo 'ffmpeg.dyn=/usr/bin' >>"${alfProps}"

sed -i -r 's|^[#]*\s*ffmpeg\.exe=.*|ffmpeg.exe=${ffmpeg.dyn}/ffmpeg|' "${alfProps}"
grep -q '^[#]*\s*ffmpeg\.exe=' "${alfProps}" || echo 'ffmpeg.exe=${ffmpeg.dyn}/ffmpeg' >>"${alfProps}"

sed -i -r 's|^[#]*\s*img\.dyn=.*|img.dyn=/usr/bin|' "${alfProps}"
grep -q '^[#]*\s*img\.dyn=' "${alfProps}" || echo 'img.dyn=/usr/bin' >>"${alfProps}"

sed -i -r 's|^[#]*\s*img\.exe=.*|img.exe=${img.dyn}/convert|' "${alfProps}"
grep -q '^[#]*\s*img\.exe=' "${alfProps}" || echo 'img.exe=${img.dyn}/convert' >>"${alfProps}"

sed -i -r 's|^[#]*\s*alfresco_user_store\.adminpassword=.*|alfresco_user_store.adminpassword='"${my_admin_pass_md4}"'|' "${alfProps}"
grep -q '^[#]*\s*alfresco_user_store\.adminpassword=' "${alfProps}" || echo "alfresco_user_store.adminpassword=${my_admin_pass_md4}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*db\.driver=.*|db.driver='"${repository_database_driv}"'|' "${alfProps}"
grep -q '^[#]*\s*db\.driver=' "${alfProps}" || echo "db.driver=${repository_database_driv}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*db\.url=.*|db.url='"${repository_database_jdbc}"'|' "${alfProps}"
grep -q '^[#]*\s*db\.url=' "${alfProps}" || echo "db.url=${repository_database_jdbc}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*db\.username=.*|db.username='"${repository_database_user}"'|' "${alfProps}"
grep -q '^[#]*\s*db\.username=' "${alfProps}" || echo "db.username=${repository_database_user}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*db\.password=.*|db.password='"${repository_database_pass}"'|' "${alfProps}"
grep -q '^[#]*\s*db\.password=' "${alfProps}" || echo "db.password=${repository_database_pass}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*db\.pool\.max=.*|db.pool.max='"${repository_database_pool_max}"'|' "${alfProps}"
grep -q '^[#]*\s*db\.pool\.max=' "${alfProps}" || echo "db.pool.max=${repository_database_pool_max}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*db\.pool\.validate\.query=.*|db.pool.validate.query='"${repository_database_pool_sql}"'|' "${alfProps}"
grep -q '^[#]*\s*db\.pool\.validate\.query=' "${alfProps}" || echo "db.pool.validate.query=${repository_database_pool_sql}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*alfresco\.protocol=.*|alfresco.protocol='"${my_prot_external}"'|' "${alfProps}"
grep -q '^[#]*\s*alfresco\.protocol=' "${alfProps}" || echo "alfresco.protocol=${my_prot_external}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*alfresco\.host=.*|alfresco.host='"${my_host_external}"'|' "${alfProps}"
grep -q '^[#]*\s*alfresco\.host=' "${alfProps}" || echo "alfresco.host=${my_host_external}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*alfresco\.port=.*|alfresco.port='"${my_port_external}"'|' "${alfProps}"
grep -q '^[#]*\s*alfresco\.port=' "${alfProps}" || echo "alfresco.port=${my_port_external}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*alfresco-pdf-renderer\.root=.*|alfresco-pdf-renderer.root='"$ALF_HOME/common/alfresco-pdf-renderer"'|' "${alfProps}"
grep -q '^[#]*\s*alfresco-pdf-renderer\.root=' "${alfProps}" || echo "alfresco-pdf-renderer.root=$ALF_HOME/common/alfresco-pdf-renderer" >>"${alfProps}"

sed -i -r 's|^[#]*\s*alfresco-pdf-renderer\.exe=.*|alfresco-pdf-renderer.exe=${alfresco-pdf-renderer.root}/alfresco-pdf-renderer|' "${alfProps}"
grep -q '^[#]*\s*alfresco-pdf-renderer\.exe=' "${alfProps}" || echo 'alfresco-pdf-renderer.exe=${alfresco-pdf-renderer.root}/alfresco-pdf-renderer' >>"${alfProps}"

sed -i -r 's|^[#]*\s*ooo\.enabled=.*|ooo.enabled=true|' "${alfProps}"
grep -q '^[#]*\s*ooo\.enabled=' "${alfProps}" || echo "ooo.enabled=true" >>"${alfProps}"

sed -i -r 's|^[#]*\s*ooo\.exe=.*|ooo.exe=|' "${alfProps}"
grep -q '^[#]*\s*ooo\.exe=' "${alfProps}" || echo "ooo.exe=" >>"${alfProps}"

sed -i -r 's|^[#]*\s*ooo\.host=.*|ooo.host='"${repository_transform_host}"'|' "${alfProps}"
grep -q '^[#]*\s*ooo\.host=' "${alfProps}" || echo "ooo.host=${repository_transform_host}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*ooo\.port=.*|ooo.port='"${repository_transform_port}"'|' "${alfProps}"
grep -q '^[#]*\s*ooo\.port=' "${alfProps}" || echo "ooo.port=${repository_transform_port}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*share\.protocol=.*|share.protocol='"${my_prot_external}"'|' "${alfProps}"
grep -q '^[#]*\s*share\.protocol=' "${alfProps}" || echo "share.protocol=${my_prot_external}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*share\.host=.*|share.host='"${my_host_external}"'|' "${alfProps}"
grep -q '^[#]*\s*share\.host=' "${alfProps}" || echo "share.host=${my_host_external}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*share\.port=.*|share.port='"${my_port_external}"'|' "${alfProps}"
grep -q '^[#]*\s*share\.port=' "${alfProps}" || echo "share.port=${my_port_external}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*solr\.host=.*|solr.host='"${repository_search_solr4_host}"'|' "${alfProps}"
grep -q '^[#]*\s*solr\.host=' "${alfProps}" || echo "solr.host=${repository_search_solr4_host}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*solr\.port=.*|solr.port='"${repository_search_solr4_port}"'|' "${alfProps}"
grep -q '^[#]*\s*solr\.port=' "${alfProps}" || echo "solr.port=${repository_search_solr4_port}" >>"${alfProps}"

sed -i -r 's|^[#]*\s*solr\.secureComms=.*|solr.secureComms=none|' "${alfProps}"
grep -q '^[#]*\s*solr\.secureComms=' "${alfProps}" || echo "solr.secureComms=none" >>"${alfProps}"

sed -i -r 's|^[#]*\s*index\.subsystem\.name=.*|index.subsystem.name=solr4|' "${alfProps}"
grep -q '^[#]*\s*index\.subsystem\.name=' "${alfProps}" || echo "index.subsystem.name=solr4" >>"${alfProps}"

### edu-sharing ########################################################################################################

xmlstarlet ed -L \
	-u '/properties/entry[@key="appid"]' -v "${my_home_appid}" \
	-u '/properties/entry[@key="authenticationwebservice"]' -v "${my_auth_external}" \
	-u '/properties/entry[@key="clientport"]' -v "${my_port_external}" \
	-u '/properties/entry[@key="clientprotocol"]' -v "${my_prot_external}" \
	-u '/properties/entry[@key="domain"]' -v "${my_host_external}" \
	-u '/properties/entry[@key="host"]' -v "${my_host_internal}" \
	-u '/properties/entry[@key="password"]' -v "${my_admin_pass}" \
	-u '/properties/entry[@key="port"]' -v "${my_port_internal}" \
	${homeProp}

[[ -n "${my_guest_user}" ]] && {
	xmlstarlet ed -L \
		-d '/properties/entry[@key="guest_username"]' \
		-s '/properties' -t elem -n "entry" -v "${my_guest_user}" \
		--var entry '$prev' \
		-i '$entry' -t attr -n "key" -v "guest_username" \
		${homeProp}
}

[[ -n "${my_guest_pass}" ]] && {
	xmlstarlet ed -L \
		-d '/properties/entry[@key="guest_password"]' \
		-s '/properties' -t elem -n "entry" -v "${my_guest_pass}" \
		--var entry '$prev' \
		-i '$entry' -t attr -n "key" -v "guest_password" \
		${homeProp}
}

[[ -n "${my_home_auth}" ]] && {
	xmlstarlet ed -L \
		-d '/properties/entry[@key="allowed_authentication_types"]' \
		-s '/properties' -t elem -n "entry" -v "${my_home_auth}" \
		--var entry '$prev' \
		-i '$entry' -t attr -n "key" -v "allowed_authentication_types" \
		${homeProp}

	[[ "${my_home_auth}" == "shibboleth" ]] && {
		sed -i -r 's|<!--\s*SAML||g' tomcat/webapps/edu-sharing/WEB-INF/web.xml
		sed -i -r 's|SAML\s*-->||g'  tomcat/webapps/edu-sharing/WEB-INF/web.xml
		xmlstarlet ed -L \
			-s '/config/values' -t elem -n 'loginUrl' -v '' \
			-d '/config/values/loginUrl[position() != 1]' \
			-u '/config/values/loginUrl' -v '/edu-sharing/shibboleth' \
			-s '/config/values' -t elem -n 'logout' -v '' \
			-d '/config/values/logout[position() != 1]' \
			-s '/config/values/logout' -t elem -n 'url' -v '' \
			-d '/config/values/logout/url[position() != 1]' \
			-u '/config/values/logout/url' -v '/edu-sharing/saml/logout' \
			-s '/config/values/logout' -t elem -n 'destroySession' -v '' \
			-d '/config/values/logout/destroySession[position() != 1]' \
			-u '/config/values/logout/destroySession' -v 'false' \
			${eduCConf}
	}
}

[[ -n "${my_home_provider}" ]] && {
	xmlstarlet ed -L \
		-d '/properties/entry[@key="remote_provider"]' \
		-s '/properties' -t elem -n "entry" -v "${my_home_provider}" \
		--var entry '$prev' \
		-i '$entry' -t attr -n "key" -v "remote_provider" \
		${homeProp}
}

[[ -n "${repository_httpclient_disablesni4hosts}" ]] && {
	hocon -f ${eduSConf} \
		set "repository.httpclient.disableSNI4Hosts" "${repository_httpclient_disablesni4hosts}"
}

[[ -n "${repository_httpclient_proxy_host}" ]] && {
	hocon -f ${eduSConf} \
		set "repository.httpclient.proxy.host" "${repository_httpclient_proxy_host}"
}

[[ -n "${repository_httpclient_proxy_nonproxyhosts}" ]] && {
	export CATALINA_OPTS="-Dhttp.nonProxyHosts=${repository_httpclient_proxy_nonproxyhosts} $CATALINA_OPTS"
	export CATALINA_OPTS="-Dhttps.nonProxyHosts=${repository_httpclient_proxy_nonproxyhosts} $CATALINA_OPTS"
	hocon -f ${eduSConf} \
		set "repository.httpclient.proxy.nonproxyhosts" "${repository_httpclient_proxy_nonproxyhosts}"
}

[[ -n "${repository_httpclient_proxy_proxyhost}" ]] && {
	export CATALINA_OPTS="-Dhttp.proxyHost=${repository_httpclient_proxy_proxyhost} $CATALINA_OPTS"
	export CATALINA_OPTS="-Dhttps.proxyHost=${repository_httpclient_proxy_proxyhost} $CATALINA_OPTS"
	hocon -f ${eduSConf} \
		set "repository.httpclient.proxy.proxyhost" "${repository_httpclient_proxy_proxyhost}"
}

[[ -n "${repository_httpclient_proxy_proxypass}" ]] && {
	export CATALINA_OPTS="-Dhttp.proxyPass=${repository_httpclient_proxy_proxypass} $CATALINA_OPTS"
	export CATALINA_OPTS="-Dhttps.proxyPass=${repository_httpclient_proxy_proxypass} $CATALINA_OPTS"
	hocon -f ${eduSConf} \
		set "repository.httpclient.proxy.proxypass" "${repository_httpclient_proxy_proxypass}"
}

[[ -n "${repository_httpclient_proxy_proxyport}" ]] && {
	export CATALINA_OPTS="-Dhttp.proxyPort=${repository_httpclient_proxy_proxyport} $CATALINA_OPTS"
	export CATALINA_OPTS="-Dhttps.proxyPort=${repository_httpclient_proxy_proxyport} $CATALINA_OPTS"
	hocon -f ${eduSConf} \
		set "repository.httpclient.proxy.proxyport" "${repository_httpclient_proxy_proxyport}"
}

[[ -n "${repository_httpclient_proxy_proxyuser}" ]] && {
	export CATALINA_OPTS="-Dhttp.proxyUser=${repository_httpclient_proxy_proxyuser} $CATALINA_OPTS"
	export CATALINA_OPTS="-Dhttps.proxyUser=${repository_httpclient_proxy_proxyuser} $CATALINA_OPTS"
	hocon -f ${eduSConf} \
		set "repository.httpclient.proxy.proxyuser" "${repository_httpclient_proxy_proxyuser}"
}

########################################################################################################################

# PLUGIN
for entrypoint in bin/plugins/plugin-*/entrypoint.sh; do
	 [[ -f $entrypoint ]] && {
	 		source "$entrypoint" || exit 1
	 }
done

########################################################################################################################

exec "$@"
