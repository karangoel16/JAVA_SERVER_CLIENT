package project;
import java.io.*;
import java.net.*;
class client_data
{
	private int client_no;
	public Socket clientSocket=null;
	public ObjectInputStream baseInputStream=null;
	public ObjectOutputStream baseOutputStream=null;
	client_data()
	{
	}
	public void setNum(int no)
	{
		client_no=no;
	}
	public int getNum()
	{
		return client_no;
	}
	public boolean delete_client()
	{
		try
		{
			if(clientSocket!=null)
			{
				baseInputStream.close();
				baseOutputStream.close();
				clientSocket.close();
				clientSocket=null;
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		return false;
	}
};
class server{
	private ServerSocket listener;
	private int port=8000;
	public client_data client_class[];
	String message;
	ObjectOutputStream out;//stream write to the socket
	ObjectInputStream in;//stream read from the socket
	//Map<Socket,String> client;
	int client_no;
	//constructor
	public server()
	{
		//client=new HashMap<Socket,String>();
		client_class=new client_data[100];
		for(int i=0;i<5;i++)
		{
			client_class[i]=new client_data();
		}
		client_no=0;
	}
    public void run() throws Exception
    {
        listener = new ServerSocket(port);
        while(true)
        {
        	//
        	try
        	{
        		Socket temp=listener.accept();
        		if(temp!=null)
        		{
        			for(int i=0;i<100;i++)
        			{
        				if(client_class[i].clientSocket==null)
        				{
        					client_no=i;
        					break;
        				}
        			}
        			client_class[client_no].setNum(client_no);
        			client_class[client_no].clientSocket=temp;
        			client_class[client_no].baseInputStream=new ObjectInputStream(client_class[client_no].clientSocket.getInputStream());
        			client_class[client_no].baseOutputStream=new ObjectOutputStream(client_class[client_no].clientSocket.getOutputStream());
        			new Handler(client_no).start();
        		}
        	}
        	catch(IOException ioe)
        	{
        		ioe.printStackTrace();
        	}
        }
    }
	private class Handler extends Thread 
    {
        	private String message;    //message received from the client
        	private String no;
        	private int client_no_;
        	//The index number of the client
        	public Handler(int client_no)
        	{
            		this.client_no_=client_no;
            		no=client_class[this.client_no_].clientSocket.getInetAddress().getHostName();
        	}
            public void run()
            {
            	try{
            		//initialize Input and Output streams
            		try
            		{
            			while(true)
            			{
            				//receive the message sent from the client
            				message = (String)client_class[client_no_].baseInputStream.readObject();
            				//System.out.println(message);
            				if(message.toUpperCase().equals("MYUSER"))
            				{
            					sendMessage("Your Client number is "+client_no_,client_class[client_no_].baseOutputStream);
            				}
            				if(message.toUpperCase().equals("ACTIVEUSER"))
            				{
            					sendMessage("Active Users are:",client_class[client_no_].baseOutputStream);
            					sendMessage("Client No. Client Name:",client_class[client_no_].baseOutputStream);
            					for (int i=0;i<=client_no;i++) 
            					{
            						if(client_class[i].clientSocket!=null)
            						sendMessage(i+" "+client_class[i].clientSocket.getInetAddress().getHostName(),client_class[client_no_].baseOutputStream);
            					}	
            				} 
            				if(message.toUpperCase().startsWith("BROADCAST"))
            				{
            					while(message.toUpperCase().startsWith("BROADCAST"))
            					{
            						//#version 2.0 edit to send particular message to every client in the network
            						message = (String)client_class[client_no_].baseInputStream.readObject();
            					}
            					
            					//version 2.0 edit added new message with client number so to used by broadcast and block cast
            					for (int i=0;i<=client_no;i++) 
                                {
                    				//version 2.0 edit added to keep the information going in NULL clients
                    				if(client_class[i].clientSocket!=null && i!=client_no_)
                    					sendMessage("Client "+client_no_+" sent:"+message,client_class[i].baseOutputStream);
                                }
            				}
            				if(message.toUpperCase().startsWith("SENDTO"))
            				{
            					while(message.toUpperCase().startsWith("SENDTO"))
            						message = (String)client_class[client_no_].baseInputStream.readObject();
            					int i=Integer.parseInt(message);
            					//check for correct i and then get things started
            					if(i>client_no && client_class[i].clientSocket==null)
            					{
            						sendMessage("INCORRECT CLIENT",client_class[client_no_].baseOutputStream);
            					}
            					else
            					{
            						sendMessage("CORRECT",client_class[client_no_].baseOutputStream);
            						sendMessage("FILE",client_class[i].baseOutputStream);
            						message=(String)client_class[client_no_].baseInputStream.readObject();//this is to get client name from our initial client and send it 
            						sendMessage(message,client_class[i].baseOutputStream);
            						message=(String)client_class[client_no_].baseInputStream.readObject();//this is the length of the bytes to be sent
            						sendMessage(message,client_class[i].baseOutputStream);
            						byte[] mybytearray=(byte[])client_class[client_no_].baseInputStream.readObject();//this is to get data
            						sendMessage(mybytearray,client_class[i].baseOutputStream); 
            						//version 4 editting some of the minor components 
            						sendMessage("client "+client_no_+" sent a file",client_class[i].baseOutputStream);
            					}
            				}
            				//Version 2.0 edit
            				if(message.toUpperCase().startsWith("BLOCKCAST"))
            				{
            					while(message.toUpperCase().startsWith("BLOCKCAST"))
            						message = (String)client_class[client_no_].baseInputStream.readObject();
            					int i=Integer.parseInt(message);
            					if(i<=client_no && client_class[i].clientSocket!=null)
            						sendMessage("CORRECT CLIENT",client_class[client_no_].baseOutputStream);
            					else
            						sendMessage("INCORRECT CLIENT",client_class[client_no_].baseOutputStream);
            					//this is to tell the client that needs to be blockcasted is incorrect
            					//to change the string into an integer
            					//need to check it version 3.0
            					message = (String)client_class[client_no_].baseInputStream.readObject();
            					for (int i1=0;i1<=client_no;i1++) 
                                {
                    				//version 2.0 edit added to keep the information going in NULL clients
                    				if(client_class[i1].clientSocket!=null && i1!=i && i1!=client_no_)
                    					sendMessage("Client "+client_no_+" sent:"+message,client_class[i1].baseOutputStream);
                                
                                }
            				}
            				if(message.toUpperCase().startsWith("FILE BROADCAST"))
            				{
            					String name,length;
            					name=(String)client_class[client_no_].baseInputStream.readObject();//this is the length of the bytes to be sent
            					length=(String)client_class[client_no_].baseInputStream.readObject();
            					byte[] mybytearray=(byte[])client_class[client_no_].baseInputStream.readObject();//this is to get data
            					System.out.println(name);
            					System.out.println(length);
        						//version 4 editting some of the minor components 
            					for (int i=0;i<=client_no;i++) 
                                {
                    				//version 2.0 edit added to keep the information going in NULL clients
                    				if(client_class[i].clientSocket!=null && i!=client_no_)
                    				{
                    					sendMessage("FILE",client_class[i].baseOutputStream);
                    					sendMessage(name,client_class[i].baseOutputStream);
                    					sendMessage(length,client_class[i].baseOutputStream);
                    					sendMessage(mybytearray,client_class[i].baseOutputStream);
                    					sendMessage("client "+client_no_+" sent a file",client_class[i].baseOutputStream);
                						
                    				}
                                }
            				}
            				if(message.toUpperCase().startsWith("EXIT"))
            				{
            					//this is to exit the client from the network
            					client_class[client_no_].delete_client();
            					
            				}
            				System.out.println("Receive message: " + message + " from client " + no);
            				}
            		}
            		catch(ClassNotFoundException classnot)
            		{
            			System.err.println("Data received in unknown format");
            			client_class[client_no_].delete_client();
            			//close connection when data received in incorrect
            		}
            	}
            	catch(IOException ioException)
            	{
            		System.out.println("Disconnect with Client " + no);
            		client_class[client_no_].delete_client();
            		//close connection when error
            	}
            	finally
            	{
            		client_class[client_no_].delete_client();
            		//Close connections
            	}
            }
        	public void sendMessage(String msg,ObjectOutputStream out)
        	{
        		try{
        			out.writeObject(msg);
        			out.flush();
        			System.out.println("Send message: " + msg + " to Client " + no);
        		}
        		catch(IOException ioException){
        			ioException.printStackTrace();
        		}
        	}
        	public void sendMessage(byte[] msg,ObjectOutputStream out)
        	{
        		try{
        			out.writeObject(msg);
        			out.flush();
        			System.out.println("Send message: " + msg + " to Client " + no);
        		}
        		catch(IOException ioException){
        			ioException.printStackTrace();
        		}
        	}
	}
	public static void main(String[] args) throws Exception
	{
		server s=new server();
		//initiating class to get run the other functions
		s.run();
		//running the other function
	}
}