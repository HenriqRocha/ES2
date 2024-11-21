O presente trabalho é fruto do projeto de Monitoria da disciplina de Programação Modular.
O mesmo tem o objetivo de orientar os primeiros passos para a criação de um projeto modularizado em Java.


## Criando o Projeto no GitLab:
    1. Criar/Entrar com uma conta na plataforma GitLab. https://gitlab.com
    2. Com o mouse, localizar e clicar no botão "Novo Projeto"
    3. Selecionar a opção de "Projeto em Branco"
    4. Informar o nome do Projeto e marcá-lo como publico.
    5. Clicar no botão "Criar projeto"

## Primeiros passos no projeto:
    1. Clonar o projeto para o computador desejado.
        1.1. Utilize da ferramenta "Git" ou similar. https://git-scm.com/
        1.2 Com a ferramenta instalada, abra um terminal e digite "git clone"
        1.3 Na página do seu projeto no GitLab localize o botão "Clone" e copie o conteúdo de sua      
        preferência, colando após o comando "git clone". 
        Ex: "git clone https://gitlab.com/unirio-pm/projeto-base-programacao-modular-unirio-echo.git"
    2. Configurar o Git para realizar commits
        2.1 No terminal digite os seguintes comandos, alterando os dados dentro das aspas.
        Ex:
            git config --global user.name "Meu nome"
            git config --global user.email "Meu email"
    3. Atualize seu repositório local e remoto com frequência
        3.1 Para atualizar o repositório remoto utilize os comandos "git add", "git commit -m", "git push"
        Ex: 
            git add arquivo_alterado.txt
            git commit -m "Alteração no arquivo"
            git push
        3.2 Para atualizar o repositório local com as alterações do repositório remoto utilize "git pull"
        Ex:
            git pull

## Criando o projeto com o Maven
    1. Escolha uma plataforma de desenvolvimento do seu interesse.
    2. Crie um projeto Java por meio da plataforma, ou utilize o Maven para criar o projeto. https://maven.apache.org/
        Obs.: Caso utilize Windows, será necessário incluir o caminho nas variáveis do ambiente.
    3. É esperado que o seu projeto tenha uma pasta 'src' com duas subpastas 'main' e 'test', alem de um arquivo pom.xml




## Utilizando o site sonarcloud.io
    1. Com a conta utilizada no GitLab, realize login
    2. Localize um sinal de "+" onde deve-se mostrar ao clicar "Analise novo projeto"
    3. Selecione a opção de Analisar novo projeto.
    4. Informe a sua chave API do gitLab
        4.1 Acesse suas configurações pessoais em seguida chaves de acesso https://gitlab.com/-/profile/personal_access_tokens
        4.2 Marque a opção API, dê um nome para a chave e clique em criar.
        4.3 Copie o código abaixo de "Seu novo token de acesso pessoal"
    5. Prossiga com a criação da organização e selecione o projeto criado no GitLab
    6. Escolha a opção recomendada "Com CI/CD do GitLab"
    7. Defina as variáveis de ambiente como informado na tela. DICA: Os links mostrados levam direto ao local de configuração do projeto.
    8. Ao clicar em continuar, selecione na proxima tela a opção maven.
    9. Substitua no arquivo pom.xml o trecho a seguir para os valores mostrados pelo Sonar ( por exemplo, <sonar.organization>[ NOME DA SUA ORGANIZAÇÃO ]</sonar.organization>)
```
    <properties>
        <sonar.organization>pedroclain</sonar.organization>
        <sonar.projectKey>pedroclain_projeto-base-programacao-modular-unirio-echo</sonar.projectKey>
    </properties>
```

        
## Configurando o AWS
    1. Crie uma conta na plataforma e realize login.
        1.1 O AWS fornece 1 ano gratuito para novos usuários
        1.2 Será necessário validar a conta adicionando um cartão de crédito
        1.3 Ao final da disciplina, exclua a conta para evitar custos
    2. Uma vez realizado o login, clique no nome do seu usuário e acesse o opção Credenciais de Segurança
       2.1 Crie uma chave de acesso e guarde a chave e o segredo que foram gerados
    3. Na barra de busca, acesse o serviço IAM e crie as seguintes roles.
        3.1 Para o serviço CodeDeploy, deve possuir a permissão AWSCodeDeployRole
        3.2 Para o serviço EC2, deve possuir as permissões AmazonEC2RoleForAWSCodeDeploy e AmazonSSMManagedInstanceCore
    4. Acesse o serviço EC2 e crie um par de chaves
    5. Crie uma instância EC2 e atribua a role criada para o EC2 e o par de chaves
        5.1 Permita o acesso via HTTP e HTTPS também
    6. Acesse o serviço CodeDeploy e crie um aplicativo e um grupo de implantação
        6.1 Atribua a role criada para o CodeDeploy
        6.2 Selecione a instância EC2 criada anteriormente para o grupo de implantação
    7. Acesse o serviço S3 e crie um bucket.
    8. Acesse novamente o serviço EC2 e selecione a instância criada. Clique em "Conectar"
        8.1 Ao acessar o terminal remoto, verifique se o codedeploy está rodando normalmente
```
            sudo service codedeploy-agent status
```
        8.2 Instale o Java 17
```
            sudo yum -y update
            sudo yum -y install wget
            wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.rpm
            sudo yum -y install ./jdk-17_linux-x64_bin.rpm
```
        8.3 Crie um arquivo de configuração para o serviço que irá rodar a aplicação
```
            sudo vi /etc/systemd/system/spring-boot-app.service
```
        8.4 No arquivo criado, cole o conteúdo abaixo *Dica, para inserir, aparte I, para sair, aparte ESC e use o comando :wq
```
        [Unit]
        Description=aplicação projeto base sprint boot
        After=network.target
        StartLimitIntervalSec=0

        [Service]
        Type=simple
        Restart=always
        RestartSec=1
        User=ec2-user
        ExecStart=sudo /usr/bin/env java -jar /opt/projeto-base-spring-boot.jar

        [Install]
        WantedBy=multi-user.target
```
    9. Crie uma variável com chave AWS_ACCESS_KEY_ID, valor (valor da chave criada inicialmente) e marque o protegido.
    10. Crie uma variável com chave AWS_SECRET_ACCESS_KEY, valor (segredo da chave criada anteriormente) e marque o protegido.
    11. Crie uma variável com chave AWS_DEFAULT_REGION, valor (região do instância, pode ser encontrado clicando na região no canto superior direito, será o código ao lado, por exemplo sa-east-1) e desmarque o protegido.
    12. Substitua no arquivo .gitlab-ci.yml o trecho a seguir com o nome dos serviços que você criou (por exemplo, --application-name [ NOME DA SUA APLICACAO ])
```
   - aws deploy push --application-name unirio-pm-projetos-base --s3-location s3://unirio-pm-projetos-base/projeto-base-spring-boot.zip --source .
   - aws deploy create-deployment --application-name unirio-pm-projetos-base --deployment-group-name projeto-base-spring-boot --s3-location bucket=unirio-pm-projetos-base,key=projeto-base-spring-boot.zip,bundleType=zip
```